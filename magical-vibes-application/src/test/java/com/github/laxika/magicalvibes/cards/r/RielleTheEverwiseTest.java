package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RielleTheEverwise.class, FaithlessLooting.class, GrizzlyBears.class})
class RielleTheEverwiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each instant and sorcery card in your graveyard")
    void powerCountsOwnInstantsAndSorceriesInGraveyard() {
        Permanent rielle = addRielleReady(player1);
        harness.setGraveyard(player1, List.of(
                new FaithlessLooting(), new FaithlessLooting(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new FaithlessLooting()));

        assertThat(gqs.getEffectivePower(gd, rielle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rielle)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws the number of cards discarded in the first discard event each turn")
    void drawsForOnlyTheFirstDiscardEventEachTurn() {
        FaithlessLooting firstLooting = new FaithlessLooting();
        FaithlessLooting secondLooting = new FaithlessLooting();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(
                new RielleTheEverwise(), firstLooting, secondLooting,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        int secondLootingIndex = findCardIndex(player1, secondLooting);
        harness.castSorcery(player1, secondLootingIndex, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    private Permanent addRielleReady(Player player) {
        return addCreatureReady(player, new RielleTheEverwise());
    }

    private int findCardIndex(Player player, Card card) {
        List<Card> hand = gd.playerHands.get(player.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card is not in hand");
    }
}
