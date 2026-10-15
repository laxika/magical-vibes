package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cryoshatter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TifaLockhart.class, Forest.class})
class TifaLockhartTest extends BaseCardTest {

    @Test
    void doublesPowerWhenLandEnters() {
        Permanent tifa = addTifa();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(2);
    }

    @Test
    void doublesCurrentPowerOnEachLandfallTrigger() {
        Permanent tifa = addTifa();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(2);

        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(4);
    }

    @Test
    @CardUsed({TifasLimitBreak.class})
    void usesPowerAtResolutionAfterPumpSpell() {
        Permanent tifa = addTifa();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(1);
        harness.setHand(player1, List.of(new TifasLimitBreak()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, tifa.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(4);
    }

    @Test
    void doublingExpiresAtEndOfTurn() {
        Permanent tifa = addTifa();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(2);
    }

    @Test
    void opponentsLandDoesNotTriggerDoubling() {
        Permanent tifa = addTifa();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(1);
    }

    @Test
    void landEnteringWithoutBeingPlayedTriggersDoubling() {
        Permanent tifa = addTifa();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(2);
    }

    @Test
    @CardUsed({Cryoshatter.class})
    void doublesNegativePower() {
        Permanent tifa = addTifa();
        harness.setHand(player1, List.of(new Cryoshatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, tifa.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(-4);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(-8);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(2);
    }

    private Permanent addTifa() {
        Permanent tifa = harness.addToBattlefieldAndReturn(player1, new TifaLockhart());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return tifa;
    }
}
