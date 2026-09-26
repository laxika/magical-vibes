package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
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

@CardUsed({MyrRetriever.class, ChromaticSphere.class, GoblinStriker.class, ElectrostaticBolt.class})
class MyrRetrieverTest extends BaseCardTest {

    private void destroyMyrRetriever(Permanent myrRetriever) {
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, myrRetriever.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When it dies, it returns another target artifact card from its controller's graveyard to hand")
    void returnsTargetArtifactCard() {
        Permanent myrRetriever = harness.addToBattlefieldAndReturn(player1, new MyrRetriever());
        Card artifact = new ChromaticSphere();
        harness.setGraveyard(player1, List.of(artifact));

        destroyMyrRetriever(myrRetriever);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chromatic Sphere");
        harness.assertInGraveyard(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("The death trigger excludes Myr Retriever itself")
    void excludesItselfFromTargets() {
        Permanent myrRetriever = harness.addToBattlefieldAndReturn(player1, new MyrRetriever());
        Card artifact = new ChromaticSphere();
        harness.setGraveyard(player1, List.of(artifact));

        destroyMyrRetriever(myrRetriever);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(artifact.getId());
        assertThat(choice.validCardIds()).doesNotContain(myrRetriever.getId());
    }

    @Test
    @DisplayName("The death trigger only targets another artifact card in its controller's graveyard")
    void requiresAnotherArtifactInOwnGraveyard() {
        Permanent myrRetriever = harness.addToBattlefieldAndReturn(player1, new MyrRetriever());
        Card nonArtifact = new GoblinStriker();
        Card opponentArtifact = new ChromaticSphere();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        destroyMyrRetriever(myrRetriever);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("If the targeted artifact leaves the graveyard before resolution, nothing is returned")
    void targetLeavingGraveyardBeforeResolutionFizzles() {
        Permanent myrRetriever = harness.addToBattlefieldAndReturn(player1, new MyrRetriever());
        Card artifact = new ChromaticSphere();
        harness.setGraveyard(player1, List.of(artifact));

        destroyMyrRetriever(myrRetriever);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of(myrRetriever.getCard()));
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Chromatic Sphere");
        harness.assertInGraveyard(player1, "Myr Retriever");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
    }
}
