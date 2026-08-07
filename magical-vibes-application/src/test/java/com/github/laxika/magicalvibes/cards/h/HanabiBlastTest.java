package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HanabiBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void dealsTwoDamageToPlayer() {
        castHanabiBlast(List.of(new HanabiBlast()), player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature")
    void dealsTwoDamageToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHanabiBlast(List.of(new HanabiBlast()), bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("With an empty hand the returned spell is the card discarded at random")
    void discardsItselfWithEmptyHand() {
        castHanabiBlast(List.of(new HanabiBlast()), player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Hanabi Blast");
    }

    @Test
    @DisplayName("With cards in hand exactly one card is discarded and the hand keeps its size")
    void discardsExactlyOneCardWithNonEmptyHand() {
        castHanabiBlast(List.of(new HanabiBlast(), new Shock(), new GrizzlyBears()), player2.getId());

        // Either Hanabi Blast discarded itself (hand keeps Shock + Bears) or it returned and one of
        // them was discarded (hand keeps Hanabi Blast + the other) — two cards either way, one in
        // the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void castHanabiBlast(List<Card> hand, java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
