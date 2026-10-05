package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindfulBiomancer.class})
class MindfulBiomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield causes its controller to gain 1 life")
    void entersAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MindfulBiomancer()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Activated ability gives +2/+2 until end of turn")
    void activatedAbilityBoosts() {
        Permanent biomancer = addCreatureReady(player1, new MindfulBiomancer());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(biomancer.getEffectivePower()).isEqualTo(4);
        assertThat(biomancer.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability can only be used once each turn")
    void activatedAbilityIsLimitedToOncePerTurn() {
        addCreatureReady(player1, new MindfulBiomancer());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability boost wears off during cleanup")
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent biomancer = addCreatureReady(player1, new MindfulBiomancer());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(biomancer.getEffectivePower()).isEqualTo(2);
        assertThat(biomancer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activation limit applies before the first activation resolves")
    void cannotActivateAgainInResponse() {
        addCreatureReady(player1, new MindfulBiomancer());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each copy can activate independently and boosts only itself")
    void separateCopiesHaveSeparateActivationLimits() {
        Permanent first = addCreatureReady(player1, new MindfulBiomancer());
        Permanent second = addCreatureReady(player1, new MindfulBiomancer());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated while summoning sick and tapped")
    void activationDoesNotRequireTapOrHaste() {
        Permanent biomancer = harness.addToBattlefieldAndReturn(player1, new MindfulBiomancer());
        biomancer.setSummoningSick(true);
        biomancer.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(biomancer.getEffectivePower()).isEqualTo(4);
        assertThat(biomancer.getEffectiveToughness()).isEqualTo(4);
        assertThat(biomancer.isTapped()).isTrue();
    }
}
