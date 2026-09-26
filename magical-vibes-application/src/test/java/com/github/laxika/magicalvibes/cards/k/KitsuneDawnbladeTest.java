package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KitsuneDawnblade.class, KitsuneBonesetter.class})
class KitsuneDawnbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability taps the target creature")
    void acceptingEtbTapsTargetCreature() {
        Permanent bonesetter = harness.addToBattlefieldAndReturn(player2, new KitsuneBonesetter());

        castKitsuneDawnblade();
        harness.handlePermanentChosen(player1, bonesetter.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bonesetter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target creature untapped")
    void decliningEtbLeavesTargetUntapped() {
        Permanent bonesetter = harness.addToBattlefieldAndReturn(player2, new KitsuneBonesetter());

        castKitsuneDawnblade();
        harness.handlePermanentChosen(player1, bonesetter.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bonesetter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When Kitsune Dawnblade becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent dawnblade = addCreatureReady(player1, new KitsuneDawnblade());
        dawnblade.setAttacking(true);
        addCreatureReady(player2, new KitsuneBonesetter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dawnblade.getPowerModifier()).isEqualTo(1);
        assertThat(dawnblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Dawnblade blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new KitsuneBonesetter());
        attacker.setAttacking(true);
        Permanent dawnblade = addCreatureReady(player2, new KitsuneDawnblade());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dawnblade.getPowerModifier()).isEqualTo(1);
        assertThat(dawnblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Dawnblade is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent dawnblade = addCreatureReady(player1, new KitsuneDawnblade());
        dawnblade.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(dawnblade.getPowerModifier()).isZero();
        assertThat(dawnblade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kitsune Dawnblade's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent dawnblade = addCreatureReady(player1, new KitsuneDawnblade());
        dawnblade.setAttacking(true);
        addCreatureReady(player2, new KitsuneBonesetter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dawnblade.getPowerModifier()).isEqualTo(1);
        assertThat(dawnblade.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dawnblade.getPowerModifier()).isZero();
        assertThat(dawnblade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kitsune Dawnblade gets only one Bushido bonus when multiple creatures block it")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent dawnblade = addCreatureReady(player1, new KitsuneDawnblade());
        dawnblade.setAttacking(true);
        addCreatureReady(player2, new KitsuneBonesetter());
        addCreatureReady(player2, new KitsuneBonesetter());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0),
                    new BlockerAssignment(1, 0)));
            resolveAllTriggers();
        });

        assertThat(dawnblade.getPowerModifier()).isEqualTo(1);
        assertThat(dawnblade.getToughnessModifier()).isEqualTo(1);
    }

    private void castKitsuneDawnblade() {
        harness.castFromHand(player1, new KitsuneDawnblade(), "{4}{W}");
        harness.passBothPriorities();
    }
}
