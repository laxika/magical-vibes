package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpringmantleCleric;
import com.github.laxika.magicalvibes.cards.v.VastwoodFortification;
import com.github.laxika.magicalvibes.cards.v.VastwoodThicket;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IridescentHornbeetle.class, IronshellBeetle.class, GrizzlyBears.class,
        SpringmantleCleric.class, VastwoodFortification.class, VastwoodThicket.class, IntoTheRoil.class})
class IridescentHornbeetleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Insect token for each +1/+1 counter put on your creatures")
    void createsTokensForCountersPutOnYourCreatures() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(target);
        putCounterOn(target);
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Insect")).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Does not count a counter put on an opponent's creature")
    void doesNotCountCounterPutOnOpponentsCreature() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(target);
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Insect")).filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("Does not count a counter put by an opponent")
    void doesNotCountCounterPutByOpponent() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new IronshellBeetle()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Insect")).filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void createsNoTokensWithoutCounterPlacements() {
        addCreatureReady(player1, new IridescentHornbeetle());

        advanceToEndStepAndResolve(player1);

        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void countsCountersPlacedBeforeHornbeetleEntered() {
        Permanent target = addCreatureReady(player1, new SpringmantleCleric());
        putFortificationCounterOn(target);
        harness.setHand(player1, List.of(new IridescentHornbeetle()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void countsEveryCounterPlacedAsCreatureEnters() {
        addCreatureReady(player1, new IridescentHornbeetle());
        harness.setHand(player1, List.of(new SpringmantleCleric()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(countPermanents(player1, "Insect")).isEqualTo(5);
    }

    @Test
    void countsCountersEvenAfterCreatureLeavesBattlefield() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player1, new SpringmantleCleric());
        putFortificationCounterOn(target);
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        advanceToEndStepAndResolve(player1);

        assertThat(countPermanents(player1, "Springmantle Cleric")).isZero();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void includesCountersPlacedInResponseToEndStepTrigger() {
        Permanent hornbeetle = addCreatureReady(player1, new IridescentHornbeetle());
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, hornbeetle.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent hornbeetle = addCreatureReady(player1, new IridescentHornbeetle());
        putFortificationCounterOn(hornbeetle);

        advanceToEndStepAndResolve(player2);

        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void eachHornbeetleCreatesTokensForTheSamePlacements() {
        Permanent hornbeetle = addCreatureReady(player1, new IridescentHornbeetle());
        addCreatureReady(player1, new IridescentHornbeetle());
        putFortificationCounterOn(hornbeetle);

        advanceToEndStepAndResolve(player1);

        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    @Test
    void doesNotCountPlacementsFromPreviousTurns() {
        Permanent hornbeetle = addCreatureReady(player1, new IridescentHornbeetle());
        harness.setLibrary(player1, List.of(new IridescentHornbeetle(), new IridescentHornbeetle()));
        harness.setLibrary(player2, List.of(new IridescentHornbeetle(), new IridescentHornbeetle()));
        putFortificationCounterOn(hornbeetle);
        advanceToEndStepAndResolve(player1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    private void putFortificationCounterOn(Permanent target) {
        harness.setHand(player1, List.of(new VastwoodFortification()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerIfHornbeetleLeavesBeforeEndStep() {
        Permanent hornbeetle = addCreatureReady(player1, new IridescentHornbeetle());
        putFortificationCounterOn(hornbeetle);
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, hornbeetle.getId());

        advanceToEndStepAndResolve(player1);

        assertThat(countPermanents(player1, "Iridescent Hornbeetle")).isZero();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void triggerStillResolvesAfterHornbeetleLeaves() {
        Permanent hornbeetle = addCreatureReady(player1, new IridescentHornbeetle());
        putFortificationCounterOn(hornbeetle);
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, hornbeetle.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Iridescent Hornbeetle")).isZero();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    private void putCounterOn(Permanent target) {
        harness.setHand(player1, List.of(new IronshellBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
