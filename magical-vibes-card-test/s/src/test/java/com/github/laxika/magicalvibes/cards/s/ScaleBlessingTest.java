package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.UpdraftElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaleBlessing.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class,
        DromokaWarrior.class, UpdraftElemental.class})
class ScaleBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Bolsters the least-tough creature and adds another counter to all controlled creatures with counters")
    void bolstersThenAddsCountersToAllCounterBearers() {
        Permanent leastToughCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent alreadyModifiedCreature = addCreatureReady(player1, new HillGiant());
        alreadyModifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ownNoncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        ownNoncreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castScaleBlessing();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(alreadyModifiedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownNoncreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when you control no creatures")
    void doesNothingWithoutCreatures() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castScaleBlessing();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Completes both instructions after a mandatory choice among tied creatures")
    void resumesAfterTiedBolsterChoice() {
        Permanent first = addCreatureReady(player1, new DromokaWarrior());
        Permanent second = addCreatureReady(player1, new DromokaWarrior());
        Permanent counterBearer = addCreatureReady(player1, new UpdraftElemental());
        counterBearer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castScaleBlessing();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(counterBearer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(counterBearer.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(counterBearer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bolsters using current toughness and adds only one counter to a creature not bolstered")
    void usesCurrentToughnessAtResolution() {
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent elemental = addCreatureReady(player1, new UpdraftElemental());
        harness.castFromHand(player1, new ScaleBlessing(), "{3}{W}");
        warrior.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.passBothPriorities();

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Leaves creatures without counters unchanged unless they are bolstered")
    void doesNotAddCountersToOtherUnmodifiedCreatures() {
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent elemental = addCreatureReady(player1, new UpdraftElemental());
        Permanent opponent = addCreatureReady(player2, new DromokaWarrior());

        castScaleBlessing();

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castScaleBlessing() {
        harness.castFromHand(player1, new ScaleBlessing(), "{3}{W}");
        harness.passBothPriorities();
    }
}
