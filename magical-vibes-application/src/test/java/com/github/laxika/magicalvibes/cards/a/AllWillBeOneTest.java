package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EvolvingAdaptive;
import com.github.laxika.magicalvibes.cards.e.ExperimentalAugury;
import com.github.laxika.magicalvibes.cards.j.JaceThePerfectedMind;
import com.github.laxika.magicalvibes.cards.p.PrologueToPhyresis;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AllWillBeOne.class, PrologueToPhyresis.class, EvolvingAdaptive.class,
        ArchfiendOfTheDross.class, JaceThePerfectedMind.class, ExperimentalAugury.class})
class AllWillBeOneTest extends BaseCardTest {

    @Test
    void dealsDamageWhenAnOpponentGetsPoisonCounter() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        harness.setHand(player1, List.of(new PrologueToPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void dealsDamageEqualToCountersPlacedOnEntry() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        harness.setHand(player1, List.of(new ArchfiendOfTheDross()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDamageAnOpponentsCreature() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArchfiendOfTheDross());
        harness.setHand(player1, List.of(new PrologueToPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageAnOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceThePerfectedMind());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new PrologueToPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetItsControllerOrItsControllersCreature() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceThePerfectedMind());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new PrologueToPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, jace.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotTriggerWhenOpponentPutsCounters() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        harness.setHand(player2, List.of(new PrologueToPhyresis()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggersWhenAddingLoyaltyToPayAnActivationCost() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceThePerfectedMind());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
    }

    @Test
    void dealsDamageForStartingLoyaltyCounters() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        harness.setHand(player1, List.of(new JaceThePerfectedMind()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatingTwoPermanentsCreatesSeparateTriggers() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent adaptive = harness.addToBattlefieldAndReturn(player1, new EvolvingAdaptive());
        adaptive.setCounterCount(CounterType.OIL, 1);
        Permanent archfiend = harness.addToBattlefieldAndReturn(player2, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 4);
        PrologueToPhyresis topCard = new PrologueToPhyresis();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(adaptive.getId(), archfiend.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, archfiend.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(archfiend.getMarkedDamage()).isEqualTo(1);
        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(archfiend.getCounterCount(CounterType.OIL)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatingTwoCounterKindsOnOnePermanentCreatesOneTwoDamageTrigger() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent adaptive = harness.addToBattlefieldAndReturn(player1, new EvolvingAdaptive());
        adaptive.setCounterCount(CounterType.OIL, 1);
        adaptive.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        PrologueToPhyresis topCard = new PrologueToPhyresis();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(adaptive.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(adaptive.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
