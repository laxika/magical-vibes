package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RemoveCountersEffect} resolved through the engine: a subject other than its source ("remove a counter from
 * enchanted creature" on an Aura whose activation cost takes it off the battlefield) and "remove all [kind] counters".
 */
@CardUsed({GrizzlyBears.class, Pacifism.class})
class RemoveCountersEffectTest extends BaseCardTest {

    /** An Aura with "Sacrifice this Aura: Remove a +1/+1 counter from enchanted creature." */
    private static Card auraWithSacrificeRemoval() {
        Card aura = new Pacifism();
        aura.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificeSelfCost(false, false),
                        new RemoveCountersEffect(Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE)),
                "Sacrifice this Aura: Remove a +1/+1 counter from enchanted creature."));
        return aura;
    }

    @Test
    @DisplayName("Removes the counter from the creature the Aura enchanted before its sacrifice cost was paid")
    void removesFromHostAfterSacrificeCost() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = addCreatureReady(player1, auraWithSacrificeRemoval());
        aura.setAttachedTo(bears.getId());

        harness.activateAbilityOfPermanent(player1, aura, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    /** Grizzly Bears with "{0}: [effect]". */
    private static Card bearsWithAbility(com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        Card bears = new GrizzlyBears();
        bears.addActivatedAbility(new ActivatedAbility(false, "{0}", List.of(effect), "{0}: Remove counters."));
        return bears;
    }

    @Test
    @DisplayName("Removing all of a kind from each creature takes every such counter, keeps the others, and counts them all")
    void removesAllOfTheKindFromEachPermanentOfTheSet() {
        Permanent own = addCreatureReady(player1, bearsWithAbility(SequenceEffect.of(
                RemoveCountersEffect.allFrom(Subjects.allCreatures(), CounterType.PLUS_ONE_PLUS_ONE),
                new DrawCardEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS)))));
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        other.setCounterCount(CounterType.CHARGE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityOfPermanent(player1, own, 0, null, null);
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 5);
    }

    @Test
    @DisplayName("\"You may remove all\" counters of a kind is asked even when the source has none of them")
    void mayRemoveAllIsAskedWithoutCounters() {
        Permanent bears = addCreatureReady(player1, bearsWithAbility(new MayEffect(
                RemoveCountersEffect.allFromSelf(CounterType.PLUS_ONE_PLUS_ONE), "Remove all +1/+1 counters?")));

        harness.activateAbilityOfPermanent(player1, bears, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("\"Remove all\" names one kind and no amount")
    void removeAllRejectsAnyKind() {
        assertThatThrownBy(() -> RemoveCountersEffect.allFromSelf(CounterType.ANY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
