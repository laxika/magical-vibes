package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.Porcuparrot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriousEgg.class, Porcuparrot.class})
class MysteriousEggTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating puts a +1/+1 counter on Mysterious Egg")
    void mutatingPutsCounterOnIt() {
        Permanent egg = addCreatureReady(player1, new MysteriousEgg());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, egg, List.of(egg.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachMutationAddsOneCounterOnlyToItsSource() {
        Permanent egg = addCreatureReady(player1, new MysteriousEgg());
        Permanent other = addCreatureReady(player1, new MysteriousEgg());
        Permanent opposing = addCreatureReady(player2, new MysteriousEgg());

        for (int i = 0; i < 2; i++) {
            harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, egg, List.of(egg.getCard()), player1.getId()));
            resolveAllTriggers();
        }

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void normalCastingDoesNotAddCounters() {
        harness.castFromHand(player1, new MysteriousEgg(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mysterious Egg")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sourceLeavingBeforeResolutionDoesNotAddCountersToAnotherEgg() {
        Permanent egg = addCreatureReady(player1, new MysteriousEgg());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, egg, List.of(egg.getCard()), player1.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(egg);
        Permanent replacement = addCreatureReady(player1, new MysteriousEgg());

        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void resolvingMutatingCreatureSpellAddsCounterToEgg() {
        Permanent egg = addCreatureReady(player1, new MysteriousEgg());
        harness.setHand(player1, List.of(new Porcuparrot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, egg.getId());
        resolveAllTriggers();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(egg);
    }
}
