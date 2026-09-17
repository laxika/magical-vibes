package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BeaconOfDestiny;
import com.github.laxika.magicalvibes.cards.d.DaruStinger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenWarhawk.class, AvenRedeemer.class, DaruStinger.class, BeaconOfDestiny.class})
class AvenWarhawkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Bird or Soldier card in your hand")
    void entersWithCounterForEachBirdOrSoldierCard() {
        harness.setHand(player1, List.of(
                new AvenWarhawk(), new AvenRedeemer(), new DaruStinger(), new AvenWarhawk(),
                new BeaconOfDestiny()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, gd.interaction
                .activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class).validCardIds());

        assertThat(findPermanent(player1, "Aven Warhawk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only Bird and Soldier cards in the controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        harness.setHand(player1, List.of(new AvenWarhawk(), new BeaconOfDestiny()));
        harness.setHand(player2, List.of(new AvenRedeemer(), new DaruStinger()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Aven Warhawk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

}
