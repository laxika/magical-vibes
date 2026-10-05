package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OranRiefOoze.class, CanopyBaloth.class})
class OranRiefOozeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you control")
    void etbPutsCounterOnTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());

        harness.setHand(player1, List.of(new OranRiefOoze()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a creature an opponent controls")
    void etbCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());

        harness.setHand(player1, List.of(new OranRiefOoze()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on each attacking creature that already has one")
    void attackCountersOnlyAttackingCreaturesWithCounters() {
        Permanent ooze = addReadyCreature(player1, new OranRiefOoze());
        Permanent counteredAttacker = addReadyCreature(player1, new CanopyBaloth());
        counteredAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncounteredAttacker = addReadyCreature(player1, new CanopyBaloth());
        Permanent counteredNonAttacker = addReadyCreature(player1, new CanopyBaloth());
        counteredNonAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(counteredAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(uncounteredAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(counteredNonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canTargetItselfWhenEnteringAnEmptyBattlefield() {
        harness.castFromHand(player1, new OranRiefOoze(), "{2}{G}");
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Oran-Rief Ooze");
        harness.handlePermanentChosen(player1, ooze.getId());
        resolveAllTriggers();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackChecksCountersAtResolutionAndIncludesItself() {
        Permanent ooze = addReadyCreature(player1, new OranRiefOoze());
        ooze.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent gainingCounter = addReadyCreature(player1, new CanopyBaloth());
        Permanent losingCounter = addReadyCreature(player1, new CanopyBaloth());
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        gainingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gainingCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(losingCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackTriggerResolvesAfterOozeLeavesTheBattlefield() {
        Permanent ooze = addReadyCreature(player1, new OranRiefOoze());
        Permanent attacker = addReadyCreature(player1, new CanopyBaloth());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(ooze);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
    @Test
    void etbDoesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        harness.setHand(player1, List.of(new OranRiefOoze()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {
        addReadyCreature(player1, new OranRiefOoze());
        Permanent attacker = addReadyCreature(player1, new CanopyBaloth());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
