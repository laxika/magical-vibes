package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilkweaverEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when your permanent left the battlefield this turn")
    void drawsCardAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        castSilkweaverElite();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw a card when no permanent left the battlefield")
    void doesNotDrawCardWithoutRevolt() {
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        castSilkweaverElite();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw a card when only an opponent's permanent left the battlefield")
    void doesNotDrawCardAfterOpponentsPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        castSilkweaverElite();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    private void castSilkweaverElite() {
        harness.setHand(player1, List.of(new SilkweaverElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
