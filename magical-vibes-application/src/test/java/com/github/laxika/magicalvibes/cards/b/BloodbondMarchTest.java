package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodbondMarch.class, GrizzlyBears.class, Ornithopter.class, HolyDay.class})
class BloodbondMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all same-name cards from every player's graveyard")
    void returnsAllSameNameCardsFromEveryPlayersGraveyard() {
        harness.addToBattlefield(player1, new BloodbondMarch());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Ornithopter()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Ornithopter")).isZero();
        assertThat(countPermanents(player2, "Ornithopter")).isZero();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");

        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature spell")
    void doesNotTriggerForNoncreatureSpell() {
        harness.addToBattlefield(player1, new BloodbondMarch());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
