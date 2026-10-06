package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamanOfSpring.class})
class ShamanOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Shaman of Spring draws a card when it enters the battlefield")
    void entersDrawsACard() {
        harness.setHand(player1, List.of(new ShamanOfSpring()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Shaman of Spring");

        // Resolve the enters-the-battlefield trigger from the stack
        harness.passBothPriorities();

        // Hand: -1 for the cast creature, +1 for the drawn card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Shaman of Spring already on the battlefield does not draw")
    void noDrawWithoutEntering() {
        harness.addToBattlefield(player1, new ShamanOfSpring());

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The enter trigger draws exactly one card for its controller only when it resolves")
    void drawWaitsForTriggerResolutionAndOnlyAffectsController() {
        ShamanOfSpring topCard = new ShamanOfSpring();
        ShamanOfSpring nextCard = new ShamanOfSpring();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new ShamanOfSpring()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shaman of Spring");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
