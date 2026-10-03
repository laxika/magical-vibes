package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BorealCentaur.class)
class BorealCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Snow activation gives Boreal Centaur +1/+1 until end of turn")
    void snowActivationBoostsSelf() {
        Permanent centaur = addCreatureReady(player1, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centaur.getEffectivePower()).isEqualTo(3);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Boreal Centaur can activate only once each turn and the boost wears off at cleanup")
    void onceEachTurnAndTemporary() {
        Permanent centaur = addCreatureReady(player1, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(centaur.getEffectivePower()).isEqualTo(2);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boreal Centaur's activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addCreatureReady(player1, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Boreal Centaur's activation does not require tapping it")
    void activationDoesNotRequireTapping() {
        Permanent centaur = addCreatureReady(player1, new BorealCentaur());
        centaur.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centaur.isTapped()).isTrue();
        assertThat(centaur.getEffectivePower()).isEqualTo(3);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boreal Centaur cannot activate with only nonsnow mana")
    void requiresSnowMana() {
        addCreatureReady(player1, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The activation limit applies before the first activation resolves")
    void cannotActivateAgainInResponse() {
        Permanent centaur = addCreatureReady(player1, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(centaur.getEffectivePower()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(centaur.getEffectivePower()).isEqualTo(3);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Boreal Centaur has its own activation limit and boosts only itself")
    void separateCopiesHaveSeparateLimits() {
        Permanent first = addCreatureReady(player1, new BorealCentaur());
        Permanent second = addCreatureReady(player1, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Boreal Centaur can activate while summoning sick on an opponent's turn")
    void canActivateWhileSummoningSickOnOpponentsTurn() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.RED, 1);

        assertThat(centaur.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centaur.getEffectivePower()).isEqualTo(3);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(3);
        assertThat(centaur.isTapped()).isFalse();
    }
}
