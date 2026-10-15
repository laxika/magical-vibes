package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NissaRevane;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGuide.class, Forest.class, GoblinShortcutter.class, NissaRevane.class})
class GoblinGuideTest extends BaseCardTest {

    private void declareAttack() {
        addCreatureReady(player1, new GoblinGuide());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Attacking puts a revealed land from the defending player's library into their hand")
    void landIsPutIntoDefendingPlayersHand() {
        Card land = new Forest();
        Card nonland = new GoblinShortcutter();
        harness.setLibrary(player2, List.of(land, nonland));

        declareAttack();

        assertThat(gd.playerHands.get(player2.getId())).contains(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("Attacking leaves a revealed nonland card on top of the defending player's library")
    void nonlandStaysOnTop() {
        Card nonland = new GoblinShortcutter();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(nonland, land));

        declareAttack();

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(nonland, land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland, land);
    }

    @Test
    @DisplayName("An empty defending library causes no zone change")
    void emptyDefendingLibraryDoesNothing() {
        gd.playerDecks.get(player2.getId()).clear();
        int handSize = gd.playerHands.get(player2.getId()).size();

        declareAttack();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void multipleGuidesEachRevealTheCurrentTopCard() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card nonland = new GoblinShortcutter();
        harness.setLibrary(player2, List.of(firstLand, secondLand, nonland));
        addCreatureReady(player1, new GoblinGuide());
        addCreatureReady(player1, new GoblinGuide());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player2.getId())).contains(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland);
    }

    @Test
    void triggerResolvesAfterGuideLeavesBattlefield() {
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        Permanent guide = addCreatureReady(player1, new GoblinGuide());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(guide);
            gd.playerGraveyards.get(player1.getId()).add(guide.getCard());
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player2.getId())).contains(land);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void triggerStillRevealsAfterAttackedPlaneswalkerLeavesBattlefield() {
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        addCreatureReady(player1, new GoblinGuide());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NissaRevane());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
            gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player2.getId())).contains(land);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void hasteAllowsAttackingOnTheTurnGuideEnters() {
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        harness.addToBattlefield(player1, new GoblinGuide());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player2.getId())).contains(land);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void nonlandIsPubliclyRevealedWithoutMovingToHand() {
        Card nonland = new GoblinShortcutter();
        harness.setLibrary(player2, List.of(nonland));

        declareAttack();

        assertThat(gameLogContains("reveals Goblin Shortcutter")).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(nonland);
    }
}
