package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkDiver.class, BraidwoodCup.class, PlatedSpider.class, RecklessAbandon.class})
class JunkDiverTest extends BaseCardTest {

    private void sacrificeJunkDiver(Permanent junkDiver) {
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), junkDiver.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When it dies, it returns another target artifact card from its controller's graveyard to hand")
    void returnsTargetArtifactCard() {
        Permanent junkDiver = harness.addToBattlefieldAndReturn(player1, new JunkDiver());
        Card artifact = new BraidwoodCup();
        harness.setGraveyard(player1, List.of(artifact));

        sacrificeJunkDiver(junkDiver);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Braidwood Cup");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(junkDiver.getCard().getId()));
    }

    @Test
    @DisplayName("The death trigger excludes Junk Diver itself")
    void excludesItselfFromTargets() {
        Permanent junkDiver = harness.addToBattlefieldAndReturn(player1, new JunkDiver());
        Card artifact = new BraidwoodCup();
        harness.setGraveyard(player1, List.of(artifact));

        sacrificeJunkDiver(junkDiver);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(artifact.getId());
        assertThat(choice.validCardIds()).doesNotContain(junkDiver.getCard().getId());
    }

    @Test
    @DisplayName("The death trigger only targets another artifact card in its controller's graveyard")
    void requiresAnotherArtifactInOwnGraveyard() {
        Permanent junkDiver = harness.addToBattlefieldAndReturn(player1, new JunkDiver());
        Card nonArtifact = new PlatedSpider();
        Card opponentArtifact = new BraidwoodCup();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        sacrificeJunkDiver(junkDiver);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("The death trigger filters out non-artifacts and artifacts in an opponent's graveyard")
    void filtersToOwnArtifactCards() {
        Permanent junkDiver = harness.addToBattlefieldAndReturn(player1, new JunkDiver());
        Card ownArtifact = new BraidwoodCup();
        Card ownNonArtifact = new PlatedSpider();
        Card opponentArtifact = new BraidwoodCup();
        harness.setGraveyard(player1, List.of(ownArtifact, ownNonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        sacrificeJunkDiver(junkDiver);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownArtifact.getId());
    }
}
