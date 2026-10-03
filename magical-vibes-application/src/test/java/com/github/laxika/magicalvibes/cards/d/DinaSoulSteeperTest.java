package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DinaSoulSteeper.class, AngelOfMercy.class, GrizzlyBears.class})
class DinaSoulSteeperTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life whenever you gain life")
    void eachOpponentLosesLifeWhenControllerGainsLife() {
        addDinaReady(player1);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing another creature gives Dina +X/+0 based on its power")
    void sacrificeAnotherCreatureBoostsBySacrificedPower() {
        Permanent dina = addDinaReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, dina)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dina)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dina's power boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dina = addDinaReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, dina)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dina)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent life gain does not trigger Dina")
    void opponentLifeGainDoesNotTrigger() {
        addDinaReady(player1);

        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Separate life gain events each trigger Dina")
    void separateLifeGainEventsEachTrigger() {
        addDinaReady(player1);

        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sacrifice uses battlefield power including counters and is paid before resolution")
    void sacrificeUsesModifiedBattlefieldPower() {
        Permanent dina = addDinaReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, dina)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dina)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dina)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dina can activate while summoning sick without tapping")
    void canActivateWhileSummoningSick() {
        Permanent dina = harness.addToBattlefieldAndReturn(player1, new DinaSoulSteeper());
        dina.setSummoningSick(true);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dina)).isEqualTo(3);
        assertThat(dina.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dina cannot sacrifice herself or an opponent's creature")
    void cannotActivateWithoutAnotherControlledCreature() {
        Permanent dina = addDinaReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dina);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addDinaReady(Player player) {
        Permanent dina = harness.addToBattlefieldAndReturn(player, new DinaSoulSteeper());
        dina.setSummoningSick(false);
        return dina;
    }
}
