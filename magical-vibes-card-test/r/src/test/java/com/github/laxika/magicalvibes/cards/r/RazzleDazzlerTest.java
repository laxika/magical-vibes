package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazzleDazzler.class, Shock.class})
class RazzleDazzlerTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell puts a counter on Razzle-Dazzler and makes it unblockable until end of turn")
    void secondSpellPutsCounterAndMakesUnblockableUntilEndOfTurn() {
        Permanent dazzler = addCreatureReady(player1, new RazzleDazzler());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dazzler.isCantBeBlocked()).isFalse();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isFalse();
    }
    @Test
    @DisplayName("Opponent spells neither trigger nor count toward the controller's second spell")
    void opponentSpellsDoNotCount() {
        Permanent dazzler = addCreatureReady(player1, new RazzleDazzler());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dazzler.isCantBeBlocked()).isFalse();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The second-spell count resets each turn while counters remain")
    void secondSpellTriggersAgainNextTurn() {
        Permanent dazzler = addCreatureReady(player1, new RazzleDazzler());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(dazzler.isCantBeBlocked()).isFalse();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isFalse();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(dazzler.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting Razzle-Dazzler as the first spell counts toward its trigger")
    void itsOwnCastCountsAsFirstSpell() {
        harness.setHand(player1, List.of(new RazzleDazzler(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent dazzler = findPermanent(player1, "Razzle-Dazzler");
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Razzle-Dazzler cast as the second spell does not trigger for itself")
    void enteringAsSecondSpellDoesNotTriggerRetroactively() {
        harness.setHand(player1, List.of(new Shock(), new RazzleDazzler(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent dazzler = findPermanent(player1, "Razzle-Dazzler");
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dazzler.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves before the second spell and works on an opponent's turn")
    void triggerResolvesBeforeSecondSpellOnOpponentsTurn() {
        Permanent dazzler = addCreatureReady(player1, new RazzleDazzler());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dazzler.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Removing the source in response does not move the effects to another Razzle-Dazzler")
    void sourceRemovedBeforeTriggerResolves() {
        Permanent removed = addCreatureReady(player1, new RazzleDazzler());
        Permanent survivor = addCreatureReady(player1, new RazzleDazzler());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, removed.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removed);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(survivor.isCantBeBlocked()).isTrue();
    }
}
