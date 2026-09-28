package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SelesnyaEvangel;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodbondMarch.class, Watchwolf.class, SelesnyaEvangel.class, BorosSignet.class})
class BloodbondMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all same-name cards from every player's graveyard")
    void returnsAllSameNameCardsFromEveryPlayersGraveyard() {
        harness.addToBattlefield(player1, new BloodbondMarch());
        harness.setGraveyard(player1, List.of(new Watchwolf(), new Watchwolf(), new SelesnyaEvangel()));
        harness.setGraveyard(player2, List.of(new Watchwolf(), new SelesnyaEvangel()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new Watchwolf(), "{G}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Watchwolf")).isEqualTo(2);
        assertThat(countPermanents(player2, "Watchwolf")).isEqualTo(1);
        assertThat(countPermanents(player1, "Selesnya Evangel")).isZero();
        assertThat(countPermanents(player2, "Selesnya Evangel")).isZero();
        harness.assertInGraveyard(player1, "Selesnya Evangel");
        harness.assertInGraveyard(player2, "Selesnya Evangel");

        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Watchwolf")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature spell")
    void doesNotTriggerForNoncreatureSpell() {
        harness.addToBattlefield(player1, new BloodbondMarch());
        harness.setGraveyard(player1, List.of(new Watchwolf()));
        harness.setGraveyard(player2, List.of(new Watchwolf()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new BorosSignet(), "{2}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Watchwolf")).isZero();
        assertThat(countPermanents(player2, "Watchwolf")).isZero();
        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertInGraveyard(player2, "Watchwolf");
    }
}
