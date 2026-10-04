package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrilledSandwalla.class})
class FrilledSandwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +2/+2 until end of turn")
    void resolvingBoosts() {
        addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sandwalla = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can only activate once each turn")
    void onlyOncePerTurn() {
        addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sandwalla = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sandwalla.getEffectivePower()).isEqualTo(1);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addSandwallaReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent sandwalla = addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sandwalla.getEffectivePower()).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Sandwalla has its own activation limit")
    void separateCopiesCanEachActivate() {
        Permanent first = addSandwallaReady(player1);
        Permanent second = addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick on the opponent turn")
    void canActivateOnOpponentTurnWhileTappedAndSummoningSick() {
        Permanent sandwalla = harness.addToBattlefieldAndReturn(player1, new FrilledSandwalla());
        sandwalla.setSummoningSick(true);
        sandwalla.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(3);
        assertThat(sandwalla.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate again on the next turn")
    void activationLimitResetsOnNextTurn() {
        Permanent sandwalla = addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addSandwallaReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FrilledSandwalla());
        perm.setSummoningSick(false);
        return perm;
    }
}
