package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RingOfRenewal.class)
class RingOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Taps, discards a card at random, and draws two cards")
    void discardsAtRandomThenDrawsTwoCards() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfRenewal());
        harness.setHand(player1, List.of(new RingOfRenewal(), new RingOfRenewal(), new RingOfRenewal()));
        harness.setLibrary(player1, List.of(new RingOfRenewal(), new RingOfRenewal()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws two cards without prompting when the hand is empty")
    void emptyHandStillDrawsTwoCards() {
        harness.addToBattlefield(player1, new RingOfRenewal());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RingOfRenewal(), new RingOfRenewal()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
