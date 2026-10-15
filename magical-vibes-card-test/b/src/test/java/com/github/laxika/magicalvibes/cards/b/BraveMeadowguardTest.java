package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraveMeadowguard.class, GiantGrowth.class, MightOfTheMeek.class, BrambleguardCaptain.class})
class BraveMeadowguardTest extends BaseCardTest {

    @Test
    void entersAndConjuresMightOfTheMeekIntoHand() {
        harness.enterBattlefieldAndReturn(player1, new BraveMeadowguard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Might of the Meek");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void valiantPutsOnlyOneCounterOnTheFirstSpellYouControlEachTurn() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, guard.getId());
        resolveAllTriggers();

        harness.castInstant(player1, 0, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsTargetingDoesNotUseUpValiantForTheTurn() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, guard.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachMeadowguardHasItsOwnValiantLimit() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, first.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, second.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantCanTriggerAgainOnTheOpponentsTurn() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player2, List.of(new GiantGrowth(), new GiantGrowth()));

        harness.castInstant(player1, 0, guard.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void friendlyTriggeredAbilityUsesTheSameValiantLimitAsSpells() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.addToBattlefield(player1, new BrambleguardCaptain());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void conjuredCardCanBeCastAndTriggersValiantBeforeItResolves() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MightOfTheMeek()));
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new BraveMeadowguard());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getOwnerId()).isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, guard.getId());
        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Might of the Meek");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
