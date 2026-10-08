package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.cards.s.SonarStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaxWaneWitness.class, AngelOfMercy.class, NightsWhisper.class, SonarStrike.class})
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
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerWhenOpponentGainsLife() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Does not trigger when its controller gains life during an opponent's turn")
    void controllerLifeGainDuringOpponentsTurnDoesNotBoost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);
        harness.setLife(player1, 20);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaxWaneWitness());
        target.tap();
        harness.setHand(player1, List.of(new SonarStrike()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Does not trigger when its controller loses life during an opponent's turn")
    void controllerLifeLossDuringOpponentsTurnDoesNotBoost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, null));
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Separate gain and loss events each add one power without changing toughness")
    void separateLifeChangesAccumulateBoosts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);
        int baseToughness = gqs.getEffectiveToughness(gd, witness);
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, null));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 2);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        resolveAllTriggers();
        harness.assertLife(player1, 22);
        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, witness)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Gaining or losing zero life does not trigger")
    void zeroLifeChangesDoNotBoost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WaxWaneWitness());
        int basePower = gqs.getEffectivePower(gd, witness);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 0, null);
        });
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, witness)).isEqualTo(basePower);
    }
}
