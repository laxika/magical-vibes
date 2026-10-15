package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SelesnyaEvangel;
import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodbondMarch.class, Watchwolf.class, SelesnyaEvangel.class, BorosSignet.class, Convolute.class})
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

    @Test
    @DisplayName("Returns the cast creature itself if it is countered before the trigger resolves")
    void returnsCounteredCreatureAlongWithExistingCopies() {
        harness.addToBattlefield(player1, new BloodbondMarch());
        harness.setGraveyard(player1, List.of(new Watchwolf()));
        harness.setGraveyard(player2, List.of(new Watchwolf()));
        Watchwolf spell = new Watchwolf();
        harness.castFromHand(player1, spell, "{G}{W}");
        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Watchwolf");
        assertThat(countPermanents(player1, "Watchwolf")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Watchwolf")).isEqualTo(2);
        assertThat(countPermanents(player2, "Watchwolf")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Watchwolf");
        harness.assertNotInGraveyard(player2, "Watchwolf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature spell does not return same-name cards")
    void doesNotReturnSameNameNoncreatureCards() {
        harness.addToBattlefield(player1, new BloodbondMarch());
        harness.setGraveyard(player1, List.of(new BorosSignet()));
        harness.setGraveyard(player2, List.of(new BorosSignet()));

        harness.castFromHand(player1, new BorosSignet(), "{2}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Boros Signet")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Boros Signet");
        harness.assertInGraveyard(player1, "Boros Signet");
        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(gd.stack).isEmpty();
    }
}
