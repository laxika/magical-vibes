package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RimefurReindeer;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaviorOfTheSleeping.class, UpTheBeanstalk.class, RimefurReindeer.class})
class SaviorOfTheSleepingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an enchantment you control dies")
    void controlledEnchantmentDyingAddsCounter() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());

        putIntoGraveyard(enchantment);

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment or a creature you control")
    void ignoresOpponentEnchantmentAndCreature() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new UpTheBeanstalk());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RimefurReindeer());

        putIntoGraveyard(opponentEnchantment);
        putIntoGraveyard(creature);

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachEnchantmentAddsACounterToEachSavior() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent enchantment1 = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        Permanent enchantment2 = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment1);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment2);
        });
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersForControlledEnchantmentOwnedByOpponent() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        UpTheBeanstalk card = new UpTheBeanstalk();
        card.setOwnerId(player2.getId());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, card);

        putIntoGraveyard(enchantment);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(card);
        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOwnedEnchantmentControlledByOpponent() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        UpTheBeanstalk card = new UpTheBeanstalk();
        card.setOwnerId(player1.getId());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, card);

        putIntoGraveyard(enchantment);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void exilingEnchantmentDoesNotTrigger() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSleeping());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, enchantment));
        resolveAllTriggers();

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void pendingTriggerDoesNotPutCounterOnReturnedSavior() {
        SaviorOfTheSleeping card = new SaviorOfTheSleeping();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment);
            harness.getPermanentRemovalService().removePermanentToExile(gd, original);
        });
        gd.exiledCards.removeIf(entry -> entry.card().getId().equals(card.getId()));
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);

        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackingDoesNotTapSavior() {
        Permanent savior = addCreatureReady(player1, new SaviorOfTheSleeping());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(savior.isAttacking()).isTrue();
        assertThat(savior.isTapped()).isFalse();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
