package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
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

class DregsOfSorrowTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two nonblack creatures and draws two cards")
    void destroysXCreaturesAndDrawsX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=2: {2}{4}{B} = 7

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castSorcery(player1, 0, 2, List.of(bearsId, giantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("X=0 destroys nothing and draws nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=0: {0}{4}{B} = 5

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Still draws X even when a target is removed before resolution")
    void drawsXEvenWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=2

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castSorcery(player1, 0, 2, List.of(bearsId, giantId));

        // One target leaves before resolution — the spell still draws X.
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Cannot target more creatures than X")
    void cannotTargetMoreThanX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=1

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(bearsId, giantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.setHand(player1, List.of(new DregsOfSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=1

        UUID blackId = harness.getPermanentId(player2, "Bog Wraith");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(blackId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }
}
