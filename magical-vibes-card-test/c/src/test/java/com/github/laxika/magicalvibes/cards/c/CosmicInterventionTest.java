package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicIntervention.class, FountainOfYouth.class, GrizzlyBears.class})
class CosmicInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles controlled permanents that would go to a graveyard and returns them next end step")
    void exilesControlledPermanentsAndReturnsThem() {
        Card creatureCard = new GrizzlyBears();
        Permanent creature = addCreatureReady(player1, creatureCard);
        Card artifactCard = new FountainOfYouth();
        Permanent artifact = new Permanent(artifactCard);
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        castCosmicIntervention();
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact);
        });

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(creatureCard, artifactCard);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(creatureCard, artifactCard);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Fountain of Youth")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .doesNotContain(creatureCard, artifactCard);
    }

    @Test
    @DisplayName("The replacement ends with the turn")
    void replacementEndsWithTurn() {
        castCosmicIntervention();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Card creatureCard = new GrizzlyBears();
        Permanent creature = addCreatureReady(player1, creatureCard);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creatureCard);
    }

    private void castCosmicIntervention() {
        harness.setHand(player1, List.of(new CosmicIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
