package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DamnablePact.class, Forest.class, GrizzlyBears.class})
class DamnablePactTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws X cards and loses X life")
    void targetPlayerDrawsAndLosesX() {
        harness.setLife(player2, 20);
        List<Card> drawnCards = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, drawnCards);
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a non-player")
    void cannotTargetNonPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DamnablePact()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        var creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
