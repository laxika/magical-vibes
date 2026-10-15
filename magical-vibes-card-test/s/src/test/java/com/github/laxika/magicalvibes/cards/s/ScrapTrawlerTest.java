package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrapTrawler.class, IchorWellspring.class, Ornithopter.class})
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

    @Test
    @DisplayName("A zero-mana artifact dying cannot return another zero-mana artifact")
    void zeroManaValueDeathHasNoLegalTarget() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new ScrapTrawler());
        Permanent dyingArtifact = addCreatureReady(player1, new Ornithopter());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dyingArtifact));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Ornithopter");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact, dyingArtifact.getCard());
    }

    @Test
    @DisplayName("Scrap Trawler cannot return an artifact from an opponent's graveyard")
    void targetsOnlyControllersGraveyard() {
        Card ownArtifact = new Ornithopter();
        Card opponentArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(ownArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        Permanent trawler = addCreatureReady(player1, new ScrapTrawler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, trawler));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownArtifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownArtifact).doesNotContain(opponentArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentArtifact);
    }

    @Test
    @DisplayName("The return ability does nothing if its target leaves the graveyard before resolution")
    void targetLeavingGraveyardIsNotReturned() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        Permanent trawler = addCreatureReady(player1, new ScrapTrawler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, trawler));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of(trawler.getCard()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths do not duplicate Scrap Trawler's own death trigger")
    void simultaneousDeathsTriggerOnlyOnceForTrawlerItself() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        Permanent trawler = addCreatureReady(player1, new ScrapTrawler());
        Permanent otherArtifact = addCreatureReady(player1, new Ornithopter());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(trawler, otherArtifact), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, trawler);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, otherArtifact);
                }));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(artifact.getId(), otherArtifact.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Scrap Trawler");
    }
}
