package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KoalaSheep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaleOfKataraAndToph.class, KoalaSheep.class})
class TaleOfKataraAndTophTest extends BaseCardTest {

    @Test
    void putsACounterOnEachCreatureTheFirstTimeItBecomesTappedDuringYourTurn() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent first = addCreatureReady(player1, new KoalaSheep());
        Permanent second = addCreatureReady(player1, new KoalaSheep());

        tapAndCollect(first);
        tapAndCollect(second);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerAgainForTheSameCreatureThatTurn() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent creature = addCreatureReady(player1, new KoalaSheep());

        tapAndCollect(creature);
        resolveAllTriggers();
        creature.untap();
        tapAndCollect(creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canTriggerAgainOnYourNextTurn() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent creature = addCreatureReady(player1, new KoalaSheep());
        tapAndCollect(creature);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        tapAndCollect(creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerDuringAnOpponentsTurn() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent creature = addCreatureReady(player1, new KoalaSheep());
        harness.forceActivePlayer(player2);
        tapAndCollect(creature);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGrantTheAbilityToOpposingCreatures() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent creature = addCreatureReady(player2, new KoalaSheep());
        harness.forceActivePlayer(player2);
        tapAndCollect(creature);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachCopyGrantsASeparateAbility() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent creature = addCreatureReady(player1, new KoalaSheep());
        tapAndCollect(creature);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void earlierTapBeforeTheEnchantmentEnteredStillCounts() {
        Permanent creature = addCreatureReady(player1, new KoalaSheep());
        tapAndCollect(creature);
        creature.untap();
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        tapAndCollect(creature);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void removingTheEnchantmentDoesNotRemoveAnAlreadyTriggeredAbility() {
        harness.addToBattlefield(player1, new TaleOfKataraAndToph());
        Permanent enchantment = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent creature = addCreatureReady(player1, new KoalaSheep());
        tapAndCollect(creature);
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void tapAndCollect(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
    }

}
