package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomFeeder.class, GrizzlyBears.class, OranRiefInvoker.class})
class FathomFeederTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability draws a card and exiles the top card of each opponent's library")
    void drawsAndExilesOpponentsTopCard() {
        Card drawnCard = new GrizzlyBears();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(exiledCard));
        harness.addToBattlefield(player1, new FathomFeeder());
        addActivationMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activated ability does not exile the controller's top card")
    void exilesOnlyOpponentsLibraries() {
        Card drawnCard = new GrizzlyBears();
        Card remainingCard = new GrizzlyBears();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.setLibrary(player2, List.of(exiledCard));
        harness.addToBattlefield(player1, new FathomFeeder());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(remainingCard, drawnCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
    }

    @Test
    void ingestExilesOnlyTheDamagedPlayersTopCardOnResolution() {
        Permanent feeder = addCreatureReady(player1, new FathomFeeder());
        feeder.setAttacking(true);
        Card topCard = new FathomFeeder();
        Card nextCard = new FathomFeeder();
        Card ownCard = new FathomFeeder();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLibrary(player1, List.of(ownCard));
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    void deathtouchKillsLargerBlockerWithoutTriggeringIngest() {
        Permanent feeder = addCreatureReady(player1, new FathomFeeder());
        feeder.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new OranRiefInvoker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Card topCard = new FathomFeeder();
        harness.setLibrary(player2, List.of(topCard));
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(feeder);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void tappedSummoningSickFeederCanActivateRepeatedly() {
        Permanent feeder = harness.addToBattlefieldAndReturn(player1, new FathomFeeder());
        feeder.tap();
        feeder.setSummoningSick(true);
        Card firstDraw = new FathomFeeder();
        Card secondDraw = new FathomFeeder();
        Card firstExile = new FathomFeeder();
        Card secondExile = new FathomFeeder();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(firstExile, secondExile));
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstExile, secondExile);
        assertThat(feeder.isTapped()).isTrue();
    }

    @Test
    void emptyOpposingLibraryDoesNotPreventDrawing() {
        Card drawnCard = new FathomFeeder();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new FathomFeeder());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
