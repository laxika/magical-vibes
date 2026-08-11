package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WinnowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target and all same-named permanents when another copy exists, then draws")
    void destroysSameNamedPermanentsAndDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new HillGiant()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castWinnow(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Draws a card without destroying a lone target")
    void drawsWithoutAnotherPermanentWithSameName() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castWinnow(targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castWinnow(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWinnow(UUID targetId) {
        harness.setHand(player1, List.of(new Winnow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);
    }
}
