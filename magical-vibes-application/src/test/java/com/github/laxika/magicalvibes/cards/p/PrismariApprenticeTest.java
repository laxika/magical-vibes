package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.c.CrackleWithPower;
import com.github.laxika.magicalvibes.cards.e.ElementalMasterpiece;
import com.github.laxika.magicalvibes.cards.t.TeachByExample;
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

@CardUsed({PrismariApprentice.class, GiantGrowth.class, GrizzlyBears.class,
        CrackleWithPower.class, ElementalMasterpiece.class, TeachByExample.class})
class PrismariApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant makes Prismari Apprentice unblockable without adding a counter")
    void castingLowManaInstantMakesApprenticeUnblockable() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting an instant or sorcery with mana value 5 or greater adds a counter")
    void castingHighManaValueSpellAddsCounter() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting and copying a high-mana-value spell each add a counter")
    void copyingHighManaValueSpellAddsAnotherCounter() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prismari Apprentice's unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(apprentice.isCantBeBlocked()).isFalse();
    }

    @Test
    void highManaValueTriggerAppliesBothEffectsInOneResolution() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chosenXCountsTowardManaValueFive() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 1, List.of(player2.getId()));
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void copiedXSpellRetainsItsManaValue() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 1);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void castingCreatureDoesNotTriggerMagecraft() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new PrismariApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isFalse();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsInstantDoesNotTriggerMagecraft() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player2, List.of(new TeachByExample()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isFalse();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void copyingLowManaValueSpellMakesApprenticeUnblockableWithoutCounters() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
