package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudkinSeer.class, Forest.class, Shock.class})
class CloudkinSeerTest extends BaseCardTest {

    @Test
    @DisplayName("When Cloudkin Seer enters, its controller draws a card")
    void etbDrawsCard() {
        harness.setHand(player1, List.of(new CloudkinSeer()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Entering without being cast draws only for Cloudkin Seer's controller")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Forest firstCard = new Forest();
        Forest secondCard = new Forest();
        harness.setLibrary(player2, List.of(firstCard, secondCard));

        harness.enterBattlefieldAndReturn(player2, new CloudkinSeer());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("The draw trigger resolves after Cloudkin Seer dies")
    void drawResolvesAfterSourceDies() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player2, ManaColor.RED, 1);
        var seer = harness.enterBattlefieldAndReturn(player1, new CloudkinSeer());

        harness.castAndResolveInstant(player2, 0, seer.getId());

        harness.assertInGraveyard(player1, "Cloudkin Seer");
        harness.assertNotOnBattlefield(player1, "Cloudkin Seer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
