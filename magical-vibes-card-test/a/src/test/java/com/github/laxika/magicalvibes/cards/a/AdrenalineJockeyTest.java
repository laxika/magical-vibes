package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.s.SkystreakEngineer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdrenalineJockey.class, ReachThroughMists.class, SkystreakEngineer.class})
class AdrenalineJockeyTest extends BaseCardTest {

    @Test
    void damagesAPlayerWhoCastsDuringAnotherPlayersTurn() {
        addJockey();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void doesNotDamageTheActivePlayerForCastingDuringTheirTurn() {
        addJockey();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void putsACounterOnItWhenYouActivateAnExhaustAbility() {
        Permanent jockey = addJockey();
        harness.addToBattlefield(player1, new SkystreakEngineer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(jockey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void damagesTheOpponentForCastingOnYourTurn() {
        addJockey();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotDamageTheOpponentForCastingOnTheirTurn() {
        addJockey();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterTriggerResolvesBeforeTheExhaustAbility() {
        Permanent jockey = addJockey();
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new SkystreakEngineer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(jockey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(jockey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotGetACounterForAnOpponentsExhaustActivation() {
        Permanent jockey = addJockey();
        Permanent engineer = harness.addToBattlefieldAndReturn(player2, new SkystreakEngineer());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(jockey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void eachJockeyGetsItsOwnCounterForTheSameExhaustActivation() {
        Permanent first = addJockey();
        Permanent second = addJockey();
        harness.addToBattlefield(player1, new SkystreakEngineer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, 0, null, null);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addJockey() {
        return harness.addToBattlefieldAndReturn(player1, new AdrenalineJockey());
    }
}
