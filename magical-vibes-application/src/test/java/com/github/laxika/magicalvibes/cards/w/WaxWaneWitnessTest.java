package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaxWaneWitness.class, AngelOfMercy.class, NightsWhisper.class})
class WaxWaneWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when its controller gains life during their turn")
    void boostsOnLifeGainDuringOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Gets +1/+0 when its controller loses life during their turn")
    void boostsOnLifeLossDuringOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);

        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Does not trigger for life changes during an opponent's turn")
    void doesNotTriggerDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower);
    }
}
