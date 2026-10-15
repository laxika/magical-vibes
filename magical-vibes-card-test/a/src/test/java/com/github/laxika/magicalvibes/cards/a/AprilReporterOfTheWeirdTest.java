package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AprilReporterOfTheWeird.class, Forest.class, GrizzlyBears.class})
class AprilReporterOfTheWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws that many cards, then discards one")
    void combatDamageDrawsThatManyThenDiscardsOne() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card cardToKeep = new GrizzlyBears();
        harness.setHand(player1, List.of(cardToKeep));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Forest()));
        addAttackingApril();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when no combat damage reaches a player")
    void doesNotTriggerWhenBlocked() {
        Permanent april = addAttackingApril();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(april);
    }

    @Test
    @DisplayName("Increased combat damage draws more cards and can discard a newly drawn card")
    void increasedDamageDrawsMoreCardsFromEmptyHand() {
        Card discarded = new Forest();
        Card secondDraw = new Forest();
        Card thirdDraw = new Forest();
        Card fourthDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(discarded, secondDraw, thirdDraw, fourthDraw, new Forest()));
        Permanent april = addAttackingApril();
        april.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveCombatAndTrigger();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(discarded, secondDraw, thirdDraw, fourthDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw, thirdDraw, fourthDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Zero combat damage does not draw or discard")
    void zeroCombatDamageDoesNotTrigger() {
        Card handCard = new Forest();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard, new Forest()));
        Permanent april = addAttackingApril();
        april.setPersistentPowerModifier(-2);

        resolveCombatAndTrigger();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("April's controller draws and discards when the second player attacks")
    void secondPlayerControllerDrawsAndDiscards() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, new Forest()));
        Permanent april = addCreatureReady(player2, new AprilReporterOfTheWeird());
        april.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstDraw);
    }

    private Permanent addAttackingApril() {
        Permanent april = addCreatureReady(player1, new AprilReporterOfTheWeird());
        april.setAttacking(true);
        return april;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
