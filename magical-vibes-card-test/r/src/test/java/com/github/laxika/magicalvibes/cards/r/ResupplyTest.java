package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Resupply.class, Forest.class})
class ResupplyTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life and draws a card")
    void gainsLifeAndDrawsCard() {
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Resupply()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the caster gains life and draws exactly one card on resolution")
    void affectsOnlyCasterOnResolution() {
        Forest drawnCard = new Forest();
        Forest remainingCard = new Forest();
        Forest opponentsCard = new Forest();
        harness.setLife(player1, 20);
        harness.setLife(player2, 12);
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new Resupply()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard, remainingCard);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        harness.assertInGraveyard(player1, "Resupply");
        assertThat(gd.stack).isEmpty();
    }
}
