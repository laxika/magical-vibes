package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target creatures and costs 6 life")
    void destroysTwoCreaturesForSixLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castSorcery(player1, 0, List.of(bearsId, giantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Casting with no targets costs no life and destroys nothing")
    void noTargetsCostsNoLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot cast with more targets than life can pay for")
    void cannotPayLifeForTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, giantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new PhyrexianPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
