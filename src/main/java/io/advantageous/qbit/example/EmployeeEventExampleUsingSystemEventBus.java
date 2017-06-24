/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.example;

import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.boon.core.Sys;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.ServiceContext.serviceContext;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * Sample implementation of inproc events-driven services using QBit.
 * @author rhightower
 * @implNote Created on 2/4/15.
 */
public class EmployeeEventExampleUsingSystemEventBus {
	
	public static final String NEW_HIRE_CHANNEL = "com.company.employee.new";
	
	public static final String PAYROLL_ADJUSTMENT_CHANNEL = "com.company.employee.payroll";
	
	
	public static void main(String... args) {
		
		System.out.println(); // cosmetics only
		
		// instantiating service controllers
		
		EmployeeHiringService hiringService = new EmployeeHiringService();
		PayrollService payrollService = new PayrollService();
		BenefitsService benefitsService = new BenefitsService();
		VolunteerService volunteerService = new VolunteerService();
		
		// creating service instances
		
		ServiceQueue hiringServiceQueue = serviceBuilder().setServiceObject(hiringService)
				.setInvokeDynamic(false).build().startServiceQueue();
		
		ServiceQueue payrollServiceQueue = serviceBuilder().setServiceObject(payrollService)
				.setInvokeDynamic(false).build().startServiceQueue();
		
		ServiceQueue benefitsServiceQueue = serviceBuilder().setServiceObject(benefitsService)
				.setInvokeDynamic(false).build().startServiceQueue();
		
		ServiceQueue volunteeringServiceQueue = serviceBuilder().setServiceObject(volunteerService)
				.setInvokeDynamic(false).build().startServiceQueue();
		
		// getting the local client of the hiring service
		
		EmployeeHiringServiceClient hiringServiceClient = hiringServiceQueue.createProxy(EmployeeHiringServiceClient.class);
		
		// hiring an employee
		
		hiringServiceClient.hireEmployee(new Employee("Rick", 1));
		
		flushServiceProxy(hiringServiceClient);
		
		Sys.sleep(5_000);
		
	}
	
	interface EmployeeHiringServiceClient {
		
		void hireEmployee(final Employee employee);
		
	}
	
	public static class Employee {
		
		final String name;
		final int id;
		
		public Employee(String name, int id) {
			this.name = name;
			this.id = id;
		}
		
		public String name() {
			return name;
		}
		
		public int id() {
			return id;
		}
		
		@Override
		public String toString() {
			return String.format("Employee{ name='%s', id=%d }", name , id);
		}
		
	}
	
	public static class EmployeeHiringService {
		
		
		public void hireEmployee(final Employee employee) {
			
			int salary = 100;
			System.out.printf("[%s - %s] EmployeeHiringService > Hired '%s'. Details: %s\n",
					Thread.currentThread().getName(), Thread.currentThread().getId(),
					employee.name(), employee);
			
			// publish the needed events
			final EventManager eventManager = serviceContext().eventManager();
			eventManager.send(NEW_HIRE_CHANNEL, employee);
			eventManager.sendArray(PAYROLL_ADJUSTMENT_CHANNEL, employee, salary);
		}
		
	}
	
	public static class BenefitsService {
		
		@OnEvent(NEW_HIRE_CHANNEL)
		public void enroll(final Employee employee) {
			
			System.out.printf("[%s - %s] BenefitsService > Enrolled '%s'.\n",
					Thread.currentThread().getName(), Thread.currentThread().getId(),
					employee.name());
		}
		
	}
	
	public static class VolunteerService {
		
		@OnEvent(NEW_HIRE_CHANNEL)
		public void invite(final Employee employee) {
			
			System.out.printf("[%s - %s] VolunteerService > Invited '%s' to the community outreach program.\n",
					Thread.currentThread().getName(), Thread.currentThread().getId(),
					employee.name());
		}
		
	}
	
	public static class PayrollService {
		
		@OnEvent(PAYROLL_ADJUSTMENT_CHANNEL)
		public void addEmployeeToPayroll(final Employee employee, int salary) {
			
			System.out.printf("[%s - %s] PayrollService > Added '%s' with salary %d.\n",
					Thread.currentThread().getName(), Thread.currentThread().getId(),
					employee.name(), salary);
		}
		
	}
	
}
