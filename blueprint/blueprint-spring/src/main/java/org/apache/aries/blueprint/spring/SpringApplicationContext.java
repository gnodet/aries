/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.aries.blueprint.spring;

import org.apache.aries.blueprint.services.ExtendedBlueprintContainer;
import org.osgi.framework.wiring.BundleWiring;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

public class SpringApplicationContext extends AbstractApplicationContext {

    private final ExtendedBlueprintContainer container;
    private final DefaultListableBeanFactory beanFactory;

    public SpringApplicationContext(ExtendedBlueprintContainer container) {
        this.container = container;
        this.beanFactory = new BlueprintBeanFactory(container);
        setClassLoader(container.getBundleContext().getBundle().adapt(BundleWiring.class).getClassLoader());
        prepareBeanFactory(beanFactory);
    }

    public void process() {
        // Invoke factory processors registered as beans in the context.
        invokeBeanFactoryPostProcessors(beanFactory);
        // Register bean processors that intercept bean creation.
        registerBeanPostProcessors(beanFactory);
    }

    @Override
    protected void refreshBeanFactory() throws BeansException, IllegalStateException {
    }

    @Override
    protected void closeBeanFactory() {
    }

    @Override
    public DefaultListableBeanFactory getBeanFactory() throws IllegalStateException {
        return beanFactory;
    }

}
