package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HormagauntHorde;
import com.github.laxika.magicalvibes.cards.p.PrimordialHydra;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFirstTyrannicWar.class, Forest.class, GrizzlyBears.class, PrimordialHydra.class,
        HormagauntHorde.class, TyranidPrime.class})
class TheFirstTyrannicWarTest extends BaseCardTest {

    @Test
    void chapterIPutsAnXCreatureOntoTheBattlefieldWithLandCountCounters() {
        addForests(3);
        harness.setHand(player1, List.of(new TheFirstTyrannicWar(), new PrimordialHydra()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent hydra = findPermanent(player1, "Primordial Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void chapterIDoesNotAddCountersToANonXCreature() {
        addForests(2);
        harness.setHand(player1, List.of(new TheFirstTyrannicWar(), new GrizzlyBears()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIIDoublesCountersOnATargetCreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void chapterIIIDoublesCountersOnATargetCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addSagaWithLore(2);

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chapterICanBeDeclined() {
        harness.setHand(player1, List.of(new TheFirstTyrannicWar(), new HormagauntHorde()));
        addSagaMana();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Hormagaunt Horde");
        harness.assertNotOnBattlefield(player1, "Hormagaunt Horde");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIWithNoCreatureInHandDoesNothing() {
        harness.setHand(player1, List.of(new TheFirstTyrannicWar(), new Forest()));
        addSagaMana();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterICountsOnlyYourLandsAtResolutionAndDoesNotSetX() {
        addForests(4);
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheFirstTyrannicWar(), new HormagauntHorde()));
        addSagaMana();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Hormagaunt Horde");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    @Test
    void chapterIWithNoLandsAddsNoCountersToAnXCreature() {
        harness.setHand(player1, List.of(new TheFirstTyrannicWar(), new HormagauntHorde()));
        addSagaMana();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Hormagaunt Horde");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIIDoublesEveryKindOfCounterAndExcludesNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 3);
        creature.setCounterCount(CounterType.FLYING, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 4);
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void chapterIIDoesNotDoubleCountersAfterTheTargetChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chapterIIICanTargetACreatureWithoutCountersAndThenSacrificesTheSaga() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TyranidPrime());
        addSagaWithLore(2);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.assertOnBattlefield(player1, "The First Tyrannic War");
        harness.passBothPriorities();

        assertThat(creature.getCounters()).isEmpty();
        harness.assertNotOnBattlefield(player1, "The First Tyrannic War");
        harness.assertInGraveyard(player1, "The First Tyrannic War");
    }

    private void addForests(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }

    private void addSagaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstTyrannicWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
