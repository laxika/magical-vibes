package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneDevourer.class, GrizzlyBears.class, Shock.class})
class BoneDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter after a creature dies this turn")
    void entersWithCountersForCreaturesThatDiedThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock(), new BoneDevourer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boneDevourer = findPermanent(player1, "Bone Devourer");
        assertThat(boneDevourer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("On death, draws cards and loses life equal to its +1/+1 counters")
    void onDeathDrawsAndLosesLifePerCounter() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent boneDevourer = addCreatureReady(player1, new BoneDevourer());
        boneDevourer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, boneDevourer));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Enters without counters when no creatures died this turn")
    void entersWithoutCountersWhenNoCreaturesDied() {
        harness.setHand(player1, List.of(new BoneDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bone Devourer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts creatures that died under either player's control")
    void countsDeathsForBothPlayers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoneDevourer());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BoneDevourer());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });
        for (int remainingTriggers = 2; remainingTriggers > 0 && !gd.stack.isEmpty(); remainingTriggers--) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new BoneDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bone Devourer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Death with no +1/+1 counters draws no cards and loses no life")
    void deathWithoutCountersDoesNothing() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BoneDevourer()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BoneDevourer());
        creature.setCounterCount(CounterType.CHARGE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The dying creature's controller draws and loses life on the opponent's turn")
    void deathBenefitsItsControllerOnOpponentsTurn() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new BoneDevourer(), new BoneDevourer()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BoneDevourer());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can be cast during the opponent's turn")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BoneDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Devourer");
    }
}
