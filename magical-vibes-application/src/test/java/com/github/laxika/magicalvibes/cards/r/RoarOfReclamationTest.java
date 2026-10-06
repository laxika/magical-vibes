package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoarOfReclamation.class, Arachnoid.class, ConjurersBauble.class, DrossCrocodile.class})
class RoarOfReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifact cards from each player's graveyard to the battlefield")
    void returnsAllArtifactsFromEachGraveyard() {
        Card playerArtifact = new Arachnoid();
        Card opponentArtifact = new Arachnoid();
        harness.setGraveyard(player1, List.of(playerArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        castRoarOfReclamation();

        harness.assertOnBattlefield(player1, playerArtifact.getName());
        harness.assertOnBattlefield(player2, opponentArtifact.getName());
        harness.assertInGraveyard(player1, "Roar of Reclamation");
        harness.assertNotInGraveyard(player1, playerArtifact.getName());
        harness.assertNotInGraveyard(player2, opponentArtifact.getName());
    }

    @Test
    @DisplayName("Does not return nonartifact cards")
    void doesNotReturnNonartifactCards() {
        Card artifact = new Arachnoid();
        Card creature = new DrossCrocodile();
        harness.setGraveyard(player1, List.of(artifact, creature));
        castRoarOfReclamation();

        harness.assertOnBattlefield(player1, artifact.getName());
        harness.assertNotOnBattlefield(player1, creature.getName());
        harness.assertNotInGraveyard(player1, artifact.getName());
        harness.assertInGraveyard(player1, creature.getName());
        harness.assertInGraveyard(player1, "Roar of Reclamation");
    }

    @Test
    @DisplayName("Returns every artifact card when multiple artifacts are in a graveyard")
    void returnsEveryArtifactCard() {
        Card firstArtifact = new Arachnoid();
        Card secondArtifact = new Arachnoid();
        Card creature = new DrossCrocodile();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact, creature));
        castRoarOfReclamation();

        assertThat(findPermanents(player1, firstArtifact.getName())).hasSize(2);
        harness.assertNotInGraveyard(player1, firstArtifact.getName());
        harness.assertInGraveyard(player1, creature.getName());
    }

    @Test
    @DisplayName("Goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castRoarOfReclamation();

        harness.assertInGraveyard(player1, "Roar of Reclamation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns noncreature artifacts untapped as well as artifact creatures")
    void returnsNoncreatureArtifacts() {
        Card artifact = new ConjurersBauble();
        Card creature = new Arachnoid();
        harness.setGraveyard(player1, List.of(artifact, creature));
        castRoarOfReclamation();

        harness.assertOnBattlefield(player1, artifact.getName());
        harness.assertOnBattlefield(player1, creature.getName());
        harness.assertNotInGraveyard(player1, artifact.getName());
        harness.assertNotInGraveyard(player1, creature.getName());
        assertThat(findPermanent(player1, artifact.getName()).isTapped()).isFalse();
        assertThat(findPermanent(player1, creature.getName()).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returns all opposing artifacts even when the caster's graveyard is empty")
    void returnsOpposingArtifactsWithEmptyCasterGraveyard() {
        Card artifact = new ConjurersBauble();
        Card creature = new Arachnoid();
        Card nonartifact = new DrossCrocodile();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(artifact, creature, nonartifact));
        castRoarOfReclamation();

        harness.assertOnBattlefield(player2, artifact.getName());
        harness.assertOnBattlefield(player2, creature.getName());
        harness.assertNotOnBattlefield(player1, artifact.getName());
        harness.assertNotOnBattlefield(player1, creature.getName());
        harness.assertNotInGraveyard(player2, artifact.getName());
        harness.assertNotInGraveyard(player2, creature.getName());
        harness.assertInGraveyard(player2, nonartifact.getName());
        harness.assertNotOnBattlefield(player2, nonartifact.getName());
        assertThat(gd.stack).isEmpty();
    }

    private void castRoarOfReclamation() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new RoarOfReclamation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
