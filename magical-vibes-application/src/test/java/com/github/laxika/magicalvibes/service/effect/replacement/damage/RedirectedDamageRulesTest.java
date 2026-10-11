package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedirectedDamageRulesTest {

    @Test
    @DisplayName("Global and source-side effects stay spent, whatever their instance or portion")
    void sourceSideEffectsStaySpent() {
        assertThat(RedirectedDamageRules.staysSpent("GlobalDamageMultiplier:3f1c")).isTrue();
        assertThat(RedirectedDamageRules.staysSpent("SourceBonus")).isTrue();
        assertThat(RedirectedDamageRules.staysSpent("SourceMultiplier@a1b2")).isTrue();
        assertThat(RedirectedDamageRules.staysSpent("SpellDamageReduction:9e@a1b2")).isTrue();
    }

    @Test
    @DisplayName("The original recipient's effects don't stay spent for the new recipient")
    void recipientSideEffectsDoNotStaySpent() {
        assertThat(RedirectedDamageRules.staysSpent("RecipientMultiplier")).isFalse();
        assertThat(RedirectedDamageRules.staysSpent("DamageReplacement")).isFalse();
        assertThat(RedirectedDamageRules.staysSpent("SourceRedirectShield")).isFalse();
        assertThat(RedirectedDamageRules.staysSpent("ChosenSourceNextDamageShield:4d")).isFalse();
    }
}
