package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.u.UntetheredExpress;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TezzeretTheSchemer.class, MindStone.class, GiantSpider.class, UntetheredExpress.class})
class TezzeretTheSchemerTest extends BaseCardTest {

    @Test
    void plusOneCreatesEtheriumCellThatProducesMana() {
        addReadyTezzeret(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent cell = findPermanent(player1, "Etherium Cell");
        assertThat(cell.getCard().hasType(CardType.ARTIFACT)).isTrue();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cell);
    }

    @Test
    void minusTwoUsesArtifactCountForPlusXMinusX() {
        addReadyTezzeret(4);
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void ultimateEmblemTargetsOnlyControlledArtifacts() {
        addReadyTezzeret(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId())
                .doesNotContain(opponentArtifact.getId(), creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
    }

    @Test
    void minusTwoWithNoArtifactsDoesNotCountOpponentsArtifacts() {
        addReadyTezzeret(4);
        harness.addToBattlefield(player2, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void minusTwoCountsArtifactsAtResolution() {
        addReadyTezzeret(4);
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLibrary(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void minusTwoDoesNotRecalculateAfterResolutionAndExpiresAtTurnEnd() {
        addReadyTezzeret(4);
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLibrary(player1, List.of(new GiantSpider()));
        harness.setLibrary(player2, List.of(new GiantSpider()));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void minusTwoCanKillCreatureWithZeroToughness() {
        addReadyTezzeret(4);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new MindStone());
        }
        GiantSpider spider = new GiantSpider();
        Permanent target = harness.addToBattlefieldAndReturn(player2, spider);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spider);
    }

    @Test
    void emblemAnimationSurvivesTurnEndAndTezzeretsDeparture() {
        addReadyTezzeret(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UntetheredExpress());
        harness.setLibrary(player2, List.of(new UntetheredExpress()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Tezzeret the Schemer")).isEmpty();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    void emblemDoesNotTriggerOnOpponentsTurn() {
        addReadyTezzeret(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UntetheredExpress());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    void emblemWithNoArtifactsDoesNotRequireTargetSelection() {
        addReadyTezzeret(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void crewingEmblemAnimatedVehicleDoesNotRestorePrintedPowerAndToughness() {
        addReadyTezzeret(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UntetheredExpress());
        addCreatureReady(player1, new GiantSpider());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    private Permanent addReadyTezzeret(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new TezzeretTheSchemer());
        permanent.setSummoningSick(false);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
