package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BaithookAngler;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.cards.n.NebelgastIntruder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShipwreckSifters.class, BaithookAngler.class, NebelgastIntruder.class,
        DawnhartRejuvenator.class, HookHauntDrifter.class, Forest.class})
class ShipwreckSiftersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing a card, then discarding a card")
    void entersAndLoots() {
        castSifters(new DawnhartRejuvenator());

        harness.assertInGraveyard(player1, "Dawnhart Rejuvenator");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Discarding a Spirit puts a +1/+1 counter on Shipwreck Sifters")
    void spiritDiscardAddsCounter() {
        Permanent sifters = castSifters(new NebelgastIntruder());

        assertThat(sifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding a card with disturb puts a +1/+1 counter on Shipwreck Sifters")
    void disturbDiscardAddsCounter() {
        Permanent sifters = castSifters(new BaithookAngler());

        assertThat(sifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding a card without Spirit or disturb does not add a counter")
    void unrelatedDiscardDoesNotAddCounter() {
        Permanent sifters = castSifters(new DawnhartRejuvenator());

        assertThat(sifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The newly drawn card can be discarded and trigger a counter")
    void discardsTheOnlyDrawnCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BaithookAngler()));

        Permanent sifters = harness.enterBattlefieldAndReturn(player1, new ShipwreckSifters());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Baithook Angler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(sifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each controlled Sifters gets its own counter for a qualifying discard")
    void multipleSiftersEachTrigger() {
        harness.addToBattlefield(player1, new ShipwreckSifters());
        castSifters(new NebelgastIntruder());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Shipwreck Sifters")).hasSize(2)
                .allSatisfy(sifters -> assertThat(sifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    @DisplayName("An opponent's qualifying discard does not trigger your Sifters")
    void opponentsDiscardDoesNotTrigger() {
        Permanent opponentsSifters = harness.addToBattlefieldAndReturn(player2, new ShipwreckSifters());

        castSifters(new BaithookAngler());
        resolveAllTriggers();

        assertThat(opponentsSifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Shipwreck Sifters").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    private Permanent castSifters(Card discardedCard) {
        harness.setHand(player1, List.of(new ShipwreckSifters(), discardedCard));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Shipwreck Sifters");
    }
}
