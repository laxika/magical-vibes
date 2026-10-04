package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoeRazerRegent.class, GrizzlyBears.class, HillGiant.class, PreyUpon.class})
class FoeRazerRegentTest extends BaseCardTest {

    @Test
    void entersAndMayFightThenPutsCountersOnItAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent regent = findPermanent(player1, "Foe-Razer Regent");
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        resolveNextEndStep();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void decliningEtbFightDoesNotScheduleCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(findPermanent(player1, "Foe-Razer Regent")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void allyCreatureFightAlsoPutsCountersOnTheFightingCreature() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new FoeRazerRegent());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(ally.getId(), target.getId()));
        resolveNextEndStep();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Foe-Razer Regent")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fightTriggersGoOnTheStackBeforeCreatingDelayedAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FoeRazerRegent());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));
        assertThat(gd.stack).extracting(entry -> entry.getControllerId())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    void bothPlayersRegentsGetCountersForFightingAndOnlyAtOneEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FoeRazerRegent());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent regent = findPermanent(player1, "Foe-Razer Regent");
        assertThat(regent.getMarkedDamage()).isEqualTo(4);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveNextEndStep();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void noFightOrCountersIfRegentLeavesBeforeItsEntryAbilityResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FoeRazerRegent());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent regent = findPermanent(player1, "Foe-Razer Regent");
        gd.playerBattlefields.get(player1.getId()).remove(regent);
        gd.playerGraveyards.get(player1.getId()).add(regent.getCard());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        resolveNextEndStep();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noFightOrCountersIfTheEntryTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FoeRazerRegent());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        Permanent regent = findPermanent(player1, "Foe-Razer Regent");
        assertThat(regent.getMarkedDamage()).isZero();
        resolveNextEndStep();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningFightLeavesBothCreaturesWithoutCountersAtTheEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FoeRazerRegent());
        castRegent();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveNextEndStep();

        Permanent regent = findPermanent(player1, "Foe-Razer Regent");
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(regent.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }

    private void castRegent() {
        harness.castFromHand(player1, new FoeRazerRegent(), "{5}{G}{G}");
    }

    private void resolveNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
