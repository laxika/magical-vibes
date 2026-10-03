package com.github.laxika.magicalvibes.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineComponentScanTest {

    @Test
    void componentScanRegistersBothTappedRevealHandlersWithoutBeanNameConflicts() {
        var beanFactory = new DefaultListableBeanFactory();
        var scanner = new ClassPathBeanDefinitionScanner(beanFactory);

        // Scan the production package without instantiating services or loading oracle data.
        // A duplicate component name must fail here before it breaks every shared test context.
        scanner.scan(GameEngineConfig.class.getPackageName());

        String normalHandler = com.github.laxika.magicalvibes.service.effect.normalfx
                .RevealTopCardMayPutMatchingOntoBattlefieldTappedEffectHandler.class.getName();
        String mayHandler = com.github.laxika.magicalvibes.service.effect.mayfx
                .RevealTopCardMayPutMatchingOntoBattlefieldTappedEffectHandler.class.getName();
        assertThat(beanFactory.getBeanDefinitionNames())
                .extracting(name -> beanFactory.getBeanDefinition(name).getBeanClassName())
                .filteredOn(className -> normalHandler.equals(className) || mayHandler.equals(className))
                .containsExactlyInAnyOrder(normalHandler, mayHandler);
    }
}
