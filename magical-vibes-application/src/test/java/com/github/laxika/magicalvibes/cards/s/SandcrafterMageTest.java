package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandcrafterMage.class, Memnite.class, HillGiant.class})
class SandcrafterMageTest extends BaseCardTest {

    @Test
    void entersAndBolstersTheCreatureWithTheLeastToughness() {
        Permanent leastToughCreature = addCreatureReady(player1, new Memnite());
        Permanent largerCreature = addCreatureReady(player1, new HillGiant());

        harness.castFromHand(player1, new SandcrafterMage(), "{2}{W}");
        resolveAllTriggers();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bolstersItselfWhenItIsTheOnlyCreature() {
        Permanent mage = harness.enterBattlefieldAndReturn(player1, new SandcrafterMage());

        resolveAllTriggers();

        assertThat(mage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesOneOfTheTiedCreaturesIncludingItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SandcrafterMage());
        Permanent mage = harness.enterBattlefieldAndReturn(player1, new SandcrafterMage());

        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), mage.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(mage.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void ignoresOpposingCreaturesWithLessToughness() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SandcrafterMage());
        Permanent mage = harness.enterBattlefieldAndReturn(player1, new SandcrafterMage());
        mage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveAllTriggers();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void determinesTheLeastToughnessAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SandcrafterMage());
        Permanent mage = harness.enterBattlefieldAndReturn(player1, new SandcrafterMage());
        assertThat(gd.stack).hasSize(1);
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
