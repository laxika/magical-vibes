package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GolbezCrystalCollector.class, GrizzlyBears.class, Ornithopter.class, Forest.class})
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

    private void addArtifacts(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
