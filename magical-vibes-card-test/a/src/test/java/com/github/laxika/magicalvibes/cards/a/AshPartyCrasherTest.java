package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BoundaryLandsRanger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshPartyCrasher.class, Forest.class, BoundaryLandsRanger.class})
class AshPartyCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when two nonland permanents entered this turn")
    void putsCounterAfterTwoNonlandPermanentsEnter() {
        Permanent ash = castAsh();
        castRanger();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without two nonland permanents")
    void doesNotTriggerWithoutTwoNonlandPermanents() {
        Permanent ash = castAsh();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent ash = castAsh();
        harness.enterBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castAsh() {
        harness.castFromHand(player1, new AshPartyCrasher(), "{R}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Ash, Party Crasher");
    }

    private void castRanger() {
        harness.castFromHand(player1, new BoundaryLandsRanger(), "{1}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent's permanents do not enable celebration")
    void doesNotCountOpponentsPermanents() {
        Permanent ash = castAsh();
        harness.enterBattlefieldAndReturn(player2, new BoundaryLandsRanger());
        harness.enterBattlefieldAndReturn(player2, new BoundaryLandsRanger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Celebration does not trigger if its condition is false when Ash attacks")
    void doesNotPutTriggerOnStackWithoutCelebration() {
        Permanent ash = castAsh();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        resolveAllTriggers();
        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Three nonland permanents still produce only one counter")
    void putsOnlyOneCounterWithMoreThanTwoEntries() {
        Permanent ash = castAsh();
        castRanger();
        castRanger();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A permanent that entered this turn still counts after leaving the battlefield")
    void countsPermanentThatHasLeftBattlefield() {
        Permanent ash = castAsh();
        castRanger();
        Permanent ranger = findPermanent(player1, "Boundary Lands Ranger");
        gd.playerBattlefields.get(player1.getId()).remove(ranger);
        harness.setGraveyard(player1, List.of(ranger.getCard()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Permanents already on the battlefield do not count as this turn's entries")
    void doesNotCountExistingPermanents() {
        Permanent ash = addCreatureReady(player1, new AshPartyCrasher());
        addCreatureReady(player1, new BoundaryLandsRanger());
        castRanger();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
