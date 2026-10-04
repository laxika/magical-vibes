package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolbezCrystalCollector.class, GrizzlyBears.class, Ornithopter.class, Forest.class, Maro.class})
class GolbezCrystalCollectorTest extends BaseCardTest {

    @Test
    void artifactEnteringTriggersSurveil() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void fourArtifactsReturnsTargetCreatureWithoutLifeLoss() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(4);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void eightArtifactsAlsoMakesOpponentsLoseReturnedCreaturesPowerInLife() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void fewerThanFourArtifactsDoesNotTriggerEndStepAbility() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(3);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void surveilCanLeaveTheCardOnTop() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void opponentsArtifactDoesNotTriggerSurveil() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsEndStepDoesNotTriggerReturn() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void droppingBelowFourArtifactsPreventsReturnAtResolution() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(4);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void droppingBelowEightArtifactsStillReturnsCreatureButDoesNotLoseLife() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachingEightArtifactsAfterTriggeringCausesLifeLoss() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(7);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void removingTheGraveyardTargetPreventsLifeLoss() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void returnedCreaturesCharacteristicDefiningPowerIsEvaluatedInHand() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        Maro maro = new Maro();
        harness.setGraveyard(player1, List.of(maro));
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(maro.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Maro");
        harness.assertLife(player2, 17);
    }

    @Test
    void returningZeroPowerCreatureDoesNotLoseLife() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertLife(player2, 20);
    }

    @Test
    void noCreatureInGraveyardPreventsLifeLoss() {
        harness.addToBattlefield(player1, new GolbezCrystalCollector());
        addArtifacts(8);
        harness.setGraveyard(player1, List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player2, 20);
    }

    private void addArtifacts(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
