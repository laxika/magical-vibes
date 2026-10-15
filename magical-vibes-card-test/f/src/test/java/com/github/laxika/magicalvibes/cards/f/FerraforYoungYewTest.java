package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestingGrounds;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerraforYoungYew.class, GrizzlyBears.class, NestingGrounds.class})
class FerraforYoungYewTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates Saprolings equal to all counters on creatures controlled by the target player")
    void etbCountsAllCountersOnTargetPlayersCreatures() {
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        targetCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        targetCreature.setCounterCount(CounterType.CHARGE, 3);

        Permanent untargetedCreature = addCreatureReady(player1, new GrizzlyBears());
        untargetedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        harness.setHand(player1, List.of(new FerraforYoungYew()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(5);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Tapped ability doubles every kind of counter on a target creature")
    void activatedAbilityDoublesEveryKindOfCounter() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        targetCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        targetCreature.setCounterCount(CounterType.CHARGE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(ferrafor.isTapped()).isTrue();
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(targetCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    void etbCountsCountersAtResolutionAndIgnoresNoncreatures() {
        Permanent creature = addCreatureReady(player2, new FerraforYoungYew());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NestingGrounds());
        land.setCounterCount(CounterType.CHARGE, 8);

        harness.setHand(player1, List.of(new FerraforYoungYew()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.setCounterCount(CounterType.STUN, 2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(5);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    void etbCanTargetControllerAndCountFerraforsOwnCounters() {
        harness.setHand(player1, List.of(new FerraforYoungYew()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, player1.getId());
        harness.passBothPriorities();
        findPermanent(player1, "Ferrafor, Young Yew").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    void etbCreatesNoTokensWhenTargetPlayerHasNoCounters() {
        harness.setHand(player1, List.of(new FerraforYoungYew()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void activatedAbilityCanTargetItselfAndUsesCurrentCounterCount() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        ferrafor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.activateAbility(player1, 0, null, ferrafor.getId());
        ferrafor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        ferrafor.setCounterCount(CounterType.STUN, 2);
        resolveAllTriggers();

        assertThat(ferrafor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(ferrafor.getCounterCount(CounterType.STUN)).isEqualTo(4);
        assertThat(ferrafor.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityDoesNotCreateCountersOnCounterlessCreature() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        harness.activateAbility(player1, 0, null, ferrafor.getId());
        resolveAllTriggers();

        assertThat(ferrafor.getTotalCounterCount()).isZero();
        assertThat(ferrafor.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityRejectsNoncreatureTarget() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NestingGrounds());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ferrafor.isTapped()).isFalse();
    }

    @Test
    void summoningSicknessPreventsTapAbility() {
        Permanent ferrafor = harness.addToBattlefieldAndReturn(player1, new FerraforYoungYew());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ferrafor.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ferrafor.isTapped()).isFalse();
    }

    @Test
    void etbSumsCountersAcrossMultipleCreatures() {
        Permanent first = addCreatureReady(player2, new FerraforYoungYew());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        second.setCounterCount(CounterType.CHARGE, 3);

        harness.setHand(player1, List.of(new FerraforYoungYew()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(5);
    }

    @Test
    void activatedAbilityDoublesMinusCounters() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        ferrafor.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.activateAbility(player1, 0, null, ferrafor.getId());
        resolveAllTriggers();

        assertThat(ferrafor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void activatedAbilityDoesNothingWhenTargetLeavesBattlefield() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        Permanent target = addCreatureReady(player2, new FerraforYoungYew());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ferrafor.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
