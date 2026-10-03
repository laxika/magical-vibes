package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeornTheFierce.class, GrizzlyBears.class, HillGiant.class})
class BeornTheFierceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other Bears you control")
    void boostsOtherBearsYouControl() {
        Permanent beorn = addCreatureReady(player1, new BeornTheFierce());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beorn)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beorn)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds a trample counter and Bear subtype before checking the draw threshold")
    void addsCounterAndSubtypeThenDrawsTwoCards() {
        addCreatureReady(player1, new BeornTheFierce());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.BEAR);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing no target skips the target effects and does not draw below three Bears")
    void choosingNoTargetSkipsTargetEffectsAndDraw() {
        addCreatureReady(player1, new BeornTheFierce());
        Permanent existingBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(existingBear.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(existingBear.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(existingBear.getGrantedSubtypes()).doesNotContain(CardSubtype.BEAR);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws with three Bears even when no target is chosen")
    void drawsWithThreeBearsAndNoTarget() {
        Permanent beorn = addCreatureReady(player1, new BeornTheFierce());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(beorn.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(firstBear.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(secondBear.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

    @Test
    @DisplayName("Becoming a Bear below the threshold does not draw and persists into the next turn")
    void grantsPersistentBearSubtypeWithoutDrawingBelowThreshold() {
        addCreatureReady(player1, new BeornTheFierce());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("An illegal sole target prevents the draw even with three other Bears")
    void illegalTargetPreventsEntireAbilityFromResolving() {
        addCreatureReady(player1, new BeornTheFierce());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(target.getGrantedSubtypes()).doesNotContain(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent beorn = addCreatureReady(player1, new BeornTheFierce());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(beorn.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

    @Test
    @DisplayName("Counts Bears at resolution after applying the subtype change")
    void checksCurrentBearCountAtResolution() {
        addCreatureReady(player1, new BeornTheFierce());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.GIANT)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
