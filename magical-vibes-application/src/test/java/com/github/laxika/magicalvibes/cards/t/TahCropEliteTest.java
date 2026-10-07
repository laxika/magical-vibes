package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TahCropElite.class})
class TahCropEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyElite(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives every creature you control +1/+1 until end of turn")
    void exertBoostsYourCreatures() {
        Permanent elite = addReadyElite(player1);
        Permanent companion = addCreatureReady(player1, new TahCropElite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, companion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, companion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent elite = addReadyElite(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(elite.isTapped()).isTrue();
        assertThat(elite.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves creatures at base stats")
    void decliningExertDoesNothing() {
        Permanent elite = addReadyElite(player1);
        Permanent companion = addCreatureReady(player1, new TahCropElite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, companion)).isEqualTo(2);
        assertThat(elite.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent elite = addReadyElite(player1);
        Permanent companion = addCreatureReady(player1, new TahCropElite());
        harness.setLibrary(player2, List.of(new TahCropElite()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, companion)).isEqualTo(3);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, companion)).isEqualTo(2);
    }

    @Test
    void exertSkipsOnlyYourNextUntapStep() {
        Permanent elite = addReadyElite(player1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(elite.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(elite.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(elite.isTapped()).isFalse();
    }

    @Test
    void exertIsPaidBeforeBoostResolves() {
        Permanent elite = addReadyElite(player1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(elite.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(2);
            assertThat(gd.stack).hasSize(1);
        });
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
    }

    @Test
    void boostExcludesOpponentAndCreaturesEnteringAfterResolution() {
        Permanent elite = addReadyElite(player1);
        Permanent opposingElite = addReadyElite(player2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        Permanent lateElite = addReadyElite(player1);

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingElite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingElite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateElite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateElite)).isEqualTo(2);
    }

    private Permanent addReadyElite(Player player) {
        return addCreatureReady(player, new TahCropElite());
    }
}
