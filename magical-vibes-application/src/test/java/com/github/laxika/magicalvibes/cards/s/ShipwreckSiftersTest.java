package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BaithookAngler;
import com.github.laxika.magicalvibes.cards.c.ClarionSpirit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
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

@CardUsed({ShipwreckSifters.class, BaithookAngler.class, ClarionSpirit.class,
        GrizzlyBears.class, HookHauntDrifter.class, Forest.class})
class ShipwreckSiftersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing a card, then discarding a card")
    void entersAndLoots() {
        castSifters(new GrizzlyBears());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Discarding a Spirit puts a +1/+1 counter on Shipwreck Sifters")
    void spiritDiscardAddsCounter() {
        Permanent sifters = castSifters(new ClarionSpirit());

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
        Permanent sifters = castSifters(new GrizzlyBears());

        assertThat(sifters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castSifters(Card discardedCard) {
        harness.setHand(player1, List.of(new ShipwreckSifters(), discardedCard));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Shipwreck Sifters");
    }
}
