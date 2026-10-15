package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkthicketWolf.class})
class DarkthicketWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Darkthicket Wolf puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Darkthicket Wolf");
    }

    @Test
    @DisplayName("Ability gives +2/+2 until end of turn")
    void abilityGivesBoost() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Second activation in same turn is rejected")
    void secondActivationInSameTurnIsRejected() {
        addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    private Permanent addReadyWolf(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DarkthicketWolf());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("The boost expires at cleanup")
    void boostExpiresAtCleanup() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(2);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A pending activation already consumes the turn's allowance")
    void pendingActivationConsumesAllowance() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Wolf has its own activation allowance and boosts only itself")
    void separateWolvesHaveIndependentAllowances() {
        Permanent first = addReadyWolf(player1);
        Permanent second = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped summoning-sick Wolf can activate on the opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        wolf.setSummoningSick(true);
        wolf.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
        assertThat(wolf.isTapped()).isTrue();
    }
}
