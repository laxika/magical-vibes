package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkshopAssistantTest extends BaseCardTest {

    private void destroyWorkshopAssistant() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When it dies, it returns another target artifact card from its controller's graveyard to hand")
    void returnsTargetArtifactCard() {
        Card workshopAssistant = new WorkshopAssistant();
        Card artifact = new TormodsCrypt();
        harness.addToBattlefield(player1, workshopAssistant);
        harness.setGraveyard(player1, List.of(artifact));

        destroyWorkshopAssistant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tormod's Crypt");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(workshopAssistant.getId()));
    }

    @Test
    @DisplayName("The death trigger excludes Workshop Assistant itself")
    void excludesItselfFromTargets() {
        Card workshopAssistant = new WorkshopAssistant();
        Card artifact = new TormodsCrypt();
        harness.addToBattlefield(player1, workshopAssistant);
        harness.setGraveyard(player1, List.of(artifact));

        destroyWorkshopAssistant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(artifact.getId());
        assertThat(choice.validCardIds()).doesNotContain(workshopAssistant.getId());
    }

    @Test
    @DisplayName("The death trigger only targets another artifact card in its controller's graveyard")
    void requiresAnotherArtifactInOwnGraveyard() {
        harness.addToBattlefield(player1, new WorkshopAssistant());
        Card nonArtifact = new GrizzlyBears();
        Card opponentArtifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        destroyWorkshopAssistant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
