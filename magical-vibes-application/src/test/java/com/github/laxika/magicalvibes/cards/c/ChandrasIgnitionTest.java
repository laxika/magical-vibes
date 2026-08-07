package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandrasIgnitionTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature deals its power to every other creature and to the opponent, but not to itself")
    void damagesEveryOtherCreatureAndOpponent() {
        // Hill Giant (3/3) is the source: it survives, both Grizzly Bears (2/2) die,
        // and the opponent takes 3.
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(giantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A creature tougher than the source's power survives")
    void toughCreatureSurvives() {
        // Grizzly Bears (2/2) as the source deals only 2 to Hill Giant (3/3).
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot choose a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
