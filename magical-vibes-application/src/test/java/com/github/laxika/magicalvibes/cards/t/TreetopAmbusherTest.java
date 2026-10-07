package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreetopAmbusher.class, MotherBear.class})
class TreetopAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets a creature I control")
    void attackTriggerRestrictsTargets() {
        Permanent ambusher = addCreatureReady(player1, new TreetopAmbusher());
        Permanent ownCreature = addCreatureReady(player1, new MotherBear());
        Permanent opponentCreature = addCreatureReady(player2, new MotherBear());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ambusher.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives the target +1/+1 until end of turn")
    void attackTriggerBoostsTargetUntilEndOfTurn() {
        addCreatureReady(player1, new TreetopAmbusher());
        Permanent ownCreature = addCreatureReady(player1, new MotherBear());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Dash grants haste and returns Treetop Ambusher to its owner's hand")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new TreetopAmbusher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent ambusher = findPermanent(player1, "Treetop Ambusher");
        assertThat(ambusher.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Treetop Ambusher");
        harness.assertNotOnBattlefield(player1, "Treetop Ambusher");
    }

    @Test
    @DisplayName("Treetop Ambusher can boost itself when it attacks")
    void attackTriggerCanTargetItself() {
        Permanent ambusher = addCreatureReady(player1, new TreetopAmbusher());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ambusher.getId());
        resolveAllTriggers();

        assertThat(ambusher.getPowerModifier()).isEqualTo(1);
        assertThat(ambusher.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Normal casting grants neither haste nor an end-step return")
    void normalCastingDoesNotApplyDash() {
        harness.setHand(player1, List.of(new TreetopAmbusher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ambusher = findPermanent(player1, "Treetop Ambusher");
        assertThat(ambusher.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Treetop Ambusher");
        harness.assertNotInHand(player1, "Treetop Ambusher");
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield trigger")
    void dashDoesNotCreateAnEnterTrigger() {
        harness.setHand(player1, List.of(new TreetopAmbusher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        harness.assertOnBattlefield(player1, "Treetop Ambusher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash creates exactly one return trigger at the next end step")
    void dashCreatesOnlyOneDelayedReturnTrigger() {
        harness.setHand(player1, List.of(new TreetopAmbusher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Treetop Ambusher");
        harness.assertNotInHand(player1, "Treetop Ambusher");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertInHand(player1, "Treetop Ambusher");
        harness.assertNotOnBattlefield(player1, "Treetop Ambusher");
    }
}
