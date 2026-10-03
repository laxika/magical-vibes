package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Flash;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CycleOfLife.class, ZhalfirinKnight.class, Boomerang.class, Flash.class})
class CycleOfLifeTest extends BaseCardTest {

    /** Casts a Zhalfirin Knight and resolves it, so it counts as "cast this turn". */
    private Permanent castKnight() {
        harness.castFromHand(player1, new ZhalfirinKnight(), "{2}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Activating returns Cycle of Life to hand and makes the creature cast this turn 0/1")
    void setsBasePowerToughnessAndBouncesItself() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, knight.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
        harness.assertInHand(player1, "Cycle of Life");
        harness.assertNotOnBattlefield(player1, "Cycle of Life");
    }

    @Test
    @DisplayName("The 0/1 lasts past end of turn — it is not an until-end-of-turn effect")
    void survivesEndOfTurnCleanup() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, knight.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("The 0/1 lasts through an opponent's next upkeep")
    void lastsThroughOpponentsNextUpkeep() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, knight.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, knight)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("At the controller's next upkeep the 0/1 ends and a +1/+1 counter is added")
    void endsAtNextUpkeepAndAddsCounter() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, knight.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature that was not cast this turn is an illegal target")
    void rejectsCreatureNotCastThisTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new ZhalfirinKnight());
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        UUID targetId = knight.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, cycleIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature permanent is an illegal target")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new CycleOfLife());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CycleOfLife());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature an opponent cast this turn is an illegal target")
    void rejectsCreatureCastByOpponent() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ZhalfirinKnight(), "{2}{W}");
        harness.passBothPriorities();
        Permanent knight = findPermanent(player2, "Zhalfirin Knight");

        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new CycleOfLife());
        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        UUID targetId = knight.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, cycleIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void basePowerToughnessChangePreservesExistingCounters() {
        Permanent knight = castKnight();
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new CycleOfLife());
        harness.activateAbility(player1, 1, null, knight.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    void counterUsesTheStackAndIsAddedOnlyOnce() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());
        harness.activateAbility(player1, 1, null, knight.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returningCreatureInResponsePreventsBothEffects() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());
        harness.activateAbility(player1, 1, null, knight.getId());
        harness.assertInHand(player1, "Cycle of Life");

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, knight.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Zhalfirin Knight");
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsPreviouslyCastCreatureReturnedWithoutCastingAgain() {
        Permanent knight = castKnight();
        harness.addToBattlefield(player1, new CycleOfLife());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, knight.getId());
        var returnedKnight = gd.playerHands.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new Flash(), returnedKnight));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Zhalfirin Knight");
        assertThat(returned.getId()).isNotEqualTo(knight.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, returned.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cycle of Life");
    }
}
