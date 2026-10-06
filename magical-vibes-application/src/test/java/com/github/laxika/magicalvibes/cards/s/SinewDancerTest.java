package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinewDancer.class, Forest.class})
class SinewDancerTest extends BaseCardTest {

    @Test
    @DisplayName("The regular ability taps a target creature")
    void regularAbilityTapsTargetCreature() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The corrupted ability taps a target creature when an opponent has three poison counters")
    void corruptedAbilityTapsTargetCreatureWhenOpponentHasThreePoisonCounters() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The corrupted ability cannot be activated without three poison counters on an opponent")
    void corruptedAbilityRequiresThreePoisonCounters() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new SinewDancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherAbilityCanTapYourOwnCreature(int abilityIndex) {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player1, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        if (abilityIndex == 0) {
            harness.addMana(player1, ManaColor.COLORLESS, 3);
        }

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherAbilityCanTargetAnAlreadyTappedCreature(int abilityIndex) {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        target.setTapped(true);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        if (abilityIndex == 0) {
            harness.addMana(player1, ManaColor.COLORLESS, 3);
        }

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherAbilityRequiresAnUntappedSource(int abilityIndex) {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        dancer.setTapped(true);
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherAbilityCannotBeActivatedWithSummoningSickness(int abilityIndex) {
        Permanent dancer = harness.addToBattlefieldAndReturn(player1, new SinewDancer());
        dancer.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void regularAbilityStillRequiresFullCostWhenCorrupted() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherAbilityRequiresWhiteMana(int abilityIndex) {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void yourPoisonCountersDoNotEnableCorruptedAbility() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void corruptedAbilityResolvesIfOpponentLosesPoisonAfterActivation() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new SinewDancer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }
}
