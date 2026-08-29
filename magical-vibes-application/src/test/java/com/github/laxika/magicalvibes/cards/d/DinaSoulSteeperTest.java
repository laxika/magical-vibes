package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addDinaReady(Player player) {
        Permanent dina = new Permanent(new DinaSoulSteeper());
        dina.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(dina);
        return dina;
    }
}
