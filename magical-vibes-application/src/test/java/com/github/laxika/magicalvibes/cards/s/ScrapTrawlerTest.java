package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("When Scrap Trawler dies, it returns a target artifact with lesser mana value")
    void returnsArtifactWithLesserManaValueWhenItDies() {
        Card artifact = new Ornithopter();
        Card equalManaValueArtifact = new ScrapTrawler();
        harness.setGraveyard(player1, List.of(artifact, equalManaValueArtifact));
        Permanent trawler = addCreatureReady(player1, new ScrapTrawler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, trawler));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Scrap Trawler");
    }

    @Test
    @DisplayName("When another artifact you control dies, Scrap Trawler returns a lesser artifact")
    void triggersForAnotherArtifactYouControl() {
        Card artifact = new Ornithopter();
        Card equalManaValueArtifact = new IchorWellspring();
        harness.setGraveyard(player1, List.of(artifact, equalManaValueArtifact));
        addCreatureReady(player1, new ScrapTrawler());
        Permanent dyingArtifact = harness.addToBattlefieldAndReturn(player1, new IchorWellspring());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dyingArtifact));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ichor Wellspring");
    }

    @Test
    @DisplayName("Scrap Trawler does not trigger for an artifact controlled by an opponent")
    void doesNotTriggerForOpponentArtifact() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new ScrapTrawler());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IchorWellspring());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opponentArtifact));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
    }
}
