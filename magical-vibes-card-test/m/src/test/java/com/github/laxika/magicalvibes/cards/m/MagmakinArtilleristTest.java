package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.w.WitsEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MagmakinArtillerist.class, WitsEnd.class, GrizzlyBears.class, Peek.class})
class MagmakinArtilleristTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to cards discarded in one event")
    void dealsDamageForMultipleCardsDiscardedInOneEvent() {
        harness.addToBattlefield(player1, new MagmakinArtillerist());
        harness.setHand(player1, List.of(new WitsEnd(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cycling deals damage from both the battlefield and cycling triggers and draws a card")
    void cyclingDealsDamageAndDraws() {
        harness.addToBattlefield(player1, new MagmakinArtillerist());
        harness.setHand(player1, List.of(new MagmakinArtillerist()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magmakin Artillerist");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cycling deals damage even without an Artillerist on the battlefield")
    void cyclingWithoutBattlefieldArtilleristDealsDamage() {
        harness.setHand(player1, List.of(new MagmakinArtillerist()));
        harness.setLibrary(player1, List.of(new MagmakinArtillerist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magmakin Artillerist");
        harness.assertInHand(player1, "Magmakin Artillerist");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent discarding does not trigger Artillerist")
    void opponentDiscardDoesNotDealDamage() {
        harness.addToBattlefield(player1, new MagmakinArtillerist());
        harness.setHand(player1, List.of(new WitsEnd()));
        harness.setHand(player2, List.of(new MagmakinArtillerist(), new MagmakinArtillerist()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Magmakin Artillerist");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
