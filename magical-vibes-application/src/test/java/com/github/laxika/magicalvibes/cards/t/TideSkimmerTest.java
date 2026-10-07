package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TideSkimmer.class, ConcordiaPegasus.class, AlpineWatchdog.class})
class TideSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when two creatures with flying attack")
    void drawsWhenTwoFlyingCreaturesAttack() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new ConcordiaPegasus());
        addCreatureReady(player1, new ConcordiaPegasus());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when only one attacking creature has flying")
    void doesNotDrawWhenOnlyOneAttackerHasFlying() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new ConcordiaPegasus());
        addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when two nonflying creatures attack")
    void doesNotDrawWhenNoAttackerHasFlying() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tide Skimmer counts itself when it attacks with another flyer")
    void countsItselfWhenAttacking() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new ConcordiaPegasus());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws only once when more than two flyers attack")
    void drawsOnlyOnceForThreeFlyingAttackers() {
        setUpBattlefieldAndLibrary();
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog()));
        addCreatureReady(player1, new ConcordiaPegasus());
        addCreatureReady(player1, new ConcordiaPegasus());
        addCreatureReady(player1, new ConcordiaPegasus());

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent attacks with two flyers")
    void doesNotTriggerForOpposingAttackers() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player2, new ConcordiaPegasus());
        addCreatureReady(player2, new ConcordiaPegasus());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw still resolves after a flying attacker leaves the battlefield")
    void drawsAfterAttackerLeavesBeforeResolution() {
        setUpBattlefieldAndLibrary();
        Permanent attacker = addCreatureReady(player1, new ConcordiaPegasus());
        addCreatureReady(player1, new ConcordiaPegasus());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1, 2)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void setUpBattlefieldAndLibrary() {
        addCreatureReady(player1, new TideSkimmer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlpineWatchdog()));
    }
}
