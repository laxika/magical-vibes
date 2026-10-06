package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BoundByMoonsilver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LudevicsTestSubject.class})
class LudevicsTestSubjectTest extends BaseCardTest {

    // ===== Card structure =====

    

    @Test
    @DisplayName("The transformed creature cannot activate the front face's ability")
    void transformedCreatureHasNoHatchlingAbility() {
        Permanent subject = addReadySubject();
        subject.setCounterCount(CounterType.HATCHLING, 4);
        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        addAbilityMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
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

        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
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

        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(4);
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
        subject.setCounterCount(CounterType.HATCHLING, 4);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        // Should have transformed
        assertThat(subject.isTransformed()).isTrue();
        assertThat(subject.getCard().getName()).isEqualTo("Ludevic's Abomination");
        // Hatchling counters should be removed
        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(0);
    }

    @Test
    @DisplayName("Ludevic's Abomination is 13/13 with trample after transform")
    void abominationHasCorrectStats() {
        Permanent subject = addReadySubject();
        subject.setCounterCount(CounterType.HATCHLING, 4);

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
        subject.setCounterCount(CounterType.HATCHLING, 3);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.isTransformed()).isFalse();
        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(4);
    }

    @Test
    @DisplayName("Transforms at more than 5 counters (e.g. 5 existing + 1 new = 6)")
    void transformsAtMoreThanFiveCounters() {
        Permanent subject = addReadySubject();
        subject.setCounterCount(CounterType.HATCHLING, 5);

        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.isTransformed()).isTrue();
        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(0);
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

        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadySubject() {
        Permanent subject = harness.addToBattlefieldAndReturn(player1, new LudevicsTestSubject());
        subject.setSummoningSick(false);
        return subject;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Already stacked activations cannot transform the creature back")
    void stackedActivationsDoNotTransformBack() {
        Permanent subject = addReadySubject();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        for (int i = 0; i < 10; i++) {
            harness.activateAbility(player1, idx, null, null);
        }
        for (int i = 0; i < 10; i++) {
            harness.passBothPriorities();
        }

        assertThat(subject.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Ludevic's Abomination");
        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick subject can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent subject = harness.addToBattlefieldAndReturn(player1, new LudevicsTestSubject());
        subject.setSummoningSick(true);
        subject.tap();
        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
        assertThat(subject.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Transformation removes hatchling counters but preserves other counters and tapped status")
    void transformationPreservesOtherCountersAndTappedStatus() {
        Permanent subject = addReadySubject();
        subject.setCounterCount(CounterType.HATCHLING, 4);
        subject.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        subject.tap();
        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.isTransformed()).isTrue();
        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isZero();
        assertThat(subject.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(subject.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, subject)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, subject)).isEqualTo(15);
    }

    @Test
    @CardUsed({BoundByMoonsilver.class})
    @DisplayName("Bound by Moonsilver prevents transformation but not hatchling counter removal")
    void transformationRestrictionIsRespected() {
        Permanent subject = addReadySubject();
        subject.setCounterCount(CounterType.HATCHLING, 4);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BoundByMoonsilver());
        aura.setAttachedTo(subject.getId());
        addAbilityMana();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(subject);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(subject.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Ludevic's Test Subject");
        assertThat(subject.getCounterCount(CounterType.HATCHLING)).isZero();
    }
}
