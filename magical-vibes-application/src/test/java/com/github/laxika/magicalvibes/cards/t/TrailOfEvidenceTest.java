package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrailOfEvidenceTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when you cast an instant")
    void investigatesForInstant() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates when you cast a sorcery")
    void investigatesForSorcery() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate for a creature spell")
    void doesNotInvestigateForCreature() {
        harness.addToBattlefield(player1, new TrailOfEvidence());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
}
