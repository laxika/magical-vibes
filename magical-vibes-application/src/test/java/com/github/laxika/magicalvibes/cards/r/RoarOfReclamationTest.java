package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoarOfReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifact cards from each player's graveyard to the battlefield")
    void returnsAllArtifactsFromEachGraveyard() {
        Card playerArtifact = new Ornithopter();
        Card opponentArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(playerArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        castRoarOfReclamation();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .singleElement().extracting(Card::getName).isEqualTo("Roar of Reclamation");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not return nonartifact cards")
    void doesNotReturnNonartifactCards() {
        Card artifact = new Ornithopter();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        castRoarOfReclamation();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature)
                .anyMatch(card -> card.getName().equals("Roar of Reclamation"));
    }

    @Test
    @DisplayName("Goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castRoarOfReclamation();

        harness.assertInGraveyard(player1, "Roar of Reclamation");
        assertThat(gd.stack).isEmpty();
    }

    private void castRoarOfReclamation() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new RoarOfReclamation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
