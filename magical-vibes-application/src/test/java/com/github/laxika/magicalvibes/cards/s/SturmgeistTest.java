package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sturmgeist.class, SilverchaseFox.class, ChapelGeist.class})
class SturmgeistTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent sturmgeist = addCreatureReady(player1, new Sturmgeist());
        harness.setHand(player1, List.of(new SilverchaseFox(), new SilverchaseFox(), new SilverchaseFox()));

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T updates dynamically as hand size changes")
    void ptUpdatesDynamically() {
        Permanent sturmgeist = addCreatureReady(player1, new Sturmgeist());
        harness.setHand(player1, List.of(new SilverchaseFox()));

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(1);

        gd.playerHands.get(player1.getId()).add(new SilverchaseFox());
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent sturmgeist = addCreatureReady(player1, new Sturmgeist());
        harness.setHand(player1, List.of(new SilverchaseFox()));
        harness.setHand(player2, List.of(new SilverchaseFox(), new SilverchaseFox(), new SilverchaseFox()));

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsCardOnCombatDamage() {
        Permanent sturmgeist = addCreatureReady(player1, new Sturmgeist());
        sturmgeist.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Resolve the combat damage trigger
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Drawing from combat damage increases P/T")
    void combatDamageDrawIncreasesPT() {
        Permanent sturmgeist = addCreatureReady(player1, new Sturmgeist());
        harness.setHand(player1, List.of(new SilverchaseFox(), new SilverchaseFox()));

        sturmgeist.setAttacking(true);
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(2);

        resolveCombat();

        // Resolve the combat damage trigger (draws a card)
        harness.passBothPriorities();

        // Hand now has 3 cards (2 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(3);
    }

    @Test
    @DisplayName("No trigger when Sturmgeist is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent sturmgeist = addCreatureReady(player1, new Sturmgeist());
        harness.setHand(player1, List.of(new SilverchaseFox(), new SilverchaseFox()));

        sturmgeist.setAttacking(true);

        // Chapel Geist (2/3 flying) blocks and kills the 2/2 Sturmgeist
        Permanent blocker = addCreatureReady(player2, new ChapelGeist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        harness.assertInGraveyard(player1, "Sturmgeist");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("The hand-size ability also works in the hand and graveyard")
    void characteristicAbilityWorksOutsideBattlefield() {
        Sturmgeist inHand = new Sturmgeist();
        Sturmgeist inGraveyard = new Sturmgeist();
        harness.setHand(player1, List.of(inHand, new SilverchaseFox()));
        harness.setHand(player2, List.of(new SilverchaseFox()));
        gd.playerGraveyards.get(player1.getId()).add(inGraveyard);

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty hand makes Sturmgeist die to state-based actions")
    void diesWithEmptyHand() {
        addCreatureReady(player1, new Sturmgeist());
        harness.setHand(player1, List.of());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Sturmgeist");
        assertThat(findPermanents(player1, "Sturmgeist")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage draws exactly one card for the second player's Sturmgeist")
    void secondPlayerDrawsOneCardRegardlessOfDamageAmount() {
        Permanent sturmgeist = addCreatureReady(player2, new Sturmgeist());
        harness.setHand(player2, List.of(new SilverchaseFox(), new SilverchaseFox(), new SilverchaseFox()));
        harness.setHand(player1, List.of(new SilverchaseFox()));
        SilverchaseFox drawnCard = new SilverchaseFox();
        harness.setLibrary(player2, List.of(drawnCard, new SilverchaseFox()));
        harness.setLife(player1, 20);
        sturmgeist.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4).contains(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(4);
    }
}
