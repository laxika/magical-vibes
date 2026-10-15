package com.github.laxika.magicalvibes.cards.p;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrowthCurve;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PensiveProfessor.class, GrizzlyBears.class, GiantGrowth.class, GrowthCurve.class, SongOfTheDryads.class})
class PensiveProfessorTest extends BaseCardTest {

    @Test
    @DisplayName("Increment placing a +1/+1 counter triggers a card draw")
    void incrementCounterDrawsCard() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        professor.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast a spell spending 2 mana — greater than the 0/2's power (0), so Increment fires.
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(professor.getPlusOnePlusOneCounters()).isEqualTo(1);
        // Cast Grizzly Bears left hand empty (handBefore - 1), then the trigger drew one back.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void multipleCountersInOnePlacementDrawOnlyOneCard() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        professor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.setLibrary(player1, List.of(new PensiveProfessor(), new PensiveProfessor(), new PensiveProfessor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, professor.getId());
        resolveAllTriggers();

        // Growth Curve has two placement events: one counter, then four counters.
        assertThat(professor.getPlusOnePlusOneCounters()).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void countersOnAnotherCreatureDoNotDrawCards() {
        harness.addToBattlefield(player1, new PensiveProfessor());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3));
        harness.setHand(player1, List.of(new GrowthCurve()));
        harness.setLibrary(player1, List.of(new PensiveProfessor(), new PensiveProfessor(), new PensiveProfessor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void equalManaSpentDoesNotTriggerIncrement() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        professor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new PensiveProfessor()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(professor.getPlusOnePlusOneCounters()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentCastingSpellDoesNotTriggerIncrement() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PensiveProfessor()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setHand(player1, List.of());

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(professor.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void incrementRechecksPowerAndToughnessWhenResolving() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        harness.setHand(player1, List.of(new PensiveProfessor()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, professor.getId());
        resolveAllTriggers();

        assertThat(professor.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void incrementDoesNothingIfSourceStopsBeingCreature() {
        Permanent professor = harness.addToBattlefieldAndReturn(player1, new PensiveProfessor());
        harness.setHand(player1, List.of(new PensiveProfessor()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);

        // Model a resolved type-changing effect while Increment is pending.
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SongOfTheDryads());
        aura.setAttachedTo(professor.getId());
        assertThat(gqs.isCreature(gd, professor)).isFalse();

        resolveAllTriggers();

        assertThat(professor.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
