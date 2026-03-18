package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LudevicsTestSubjectTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one activated ability with correct effect")
    void hasCorrectAbility() {
        LudevicsTestSubject card = new LudevicsTestSubject();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}{U}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(PutCounterOnSelfThenTransformIfThresholdEffect.class);

        var effect = (PutCounterOnSelfThenTransformIfThresholdEffect) ability.getEffects().getFirst();
        assertThat(effect.counterType()).isEqualTo(CounterType.HATCHLING);
        assertThat(effect.threshold()).isEqualTo(5);
    }

    @Test
    @DisplayName("Has a back face card configured")
    void hasBackFace() {
        LudevicsTestSubject card = new LudevicsTestSubject();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("LudevicsAbomination");
    }

    // ===== Activated ability: put hatchling counter =====

    @Test
    @DisplayName("Activating ability puts a hatchling counter on the creature")
    void abilityAddsHatchlingCounter() {
        Permanent subject = addReadySubject();
        addAbilityMana();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.getHatchlingCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can accumulate multiple hatchling counters")
    void abilityAccumulatesCounters() {
        Permanent subject = addReadySubject();

        for (int i = 0; i < 4; i++) {
            addAbilityMana();
            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
            harness.activateAbility(player1, idx, null, null);
            harness.passBothPriorities();
        }

        assertThat(subject.getHatchlingCounters()).isEqualTo(4);
        assertThat(subject.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void abilityRequiresMana() {
        Permanent subject = addReadySubject();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Transform at 5 counters =====

    @Test
    @DisplayName("Reaching 5 hatchling counters removes all and transforms into Ludevic's Abomination")
    void transformsAtFiveCounters() {
        Permanent subject = addReadySubject();
        subject.setHatchlingCounters(4);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        // Should have transformed
        assertThat(subject.isTransformed()).isTrue();
        assertThat(subject.getCard().getName()).isEqualTo("Ludevic's Abomination");
        // Hatchling counters should be removed
        assertThat(subject.getHatchlingCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ludevic's Abomination is 13/13 with trample after transform")
    void abominationHasCorrectStats() {
        Permanent subject = addReadySubject();
        subject.setHatchlingCounters(4);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, subject)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, subject)).isEqualTo(13);
        assertThat(subject.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Does not transform at exactly 4 counters (needs 5)")
    void doesNotTransformAtFourCounters() {
        Permanent subject = addReadySubject();
        subject.setHatchlingCounters(3);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.isTransformed()).isFalse();
        assertThat(subject.getHatchlingCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("Transforms at more than 5 counters (e.g. 5 existing + 1 new = 6)")
    void transformsAtMoreThanFiveCounters() {
        Permanent subject = addReadySubject();
        subject.setHatchlingCounters(5);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.isTransformed()).isTrue();
        assertThat(subject.getHatchlingCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability does not require tap — can activate multiple times per turn")
    void abilityDoesNotRequireTap() {
        Permanent subject = addReadySubject();

        // Activate twice in one turn
        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        addAbilityMana();
        idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.getHatchlingCounters()).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadySubject() {
        harness.addToBattlefield(player1, new LudevicsTestSubject());
        Permanent subject = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ludevic's Test Subject"))
                .findFirst().orElseThrow();
        subject.setSummoningSick(false);
        return subject;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
