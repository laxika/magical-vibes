package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VronosMaskedInquisitor.class, KarnScionOfUrza.class, JaceBeleren.class,
        GrizzlyBears.class, Millstone.class, Island.class, Ornithopter.class})
class VronosMaskedInquisitorTest extends BaseCardTest {

    @Test
    void plusOnePhasesOutUpToTwoOtherPlaneswalkersAtNextEndStep() {
        Permanent vronos = addReadyVronos(player1, 4);
        Permanent karn = addReady(player1, new KarnScionOfUrza());
        Permanent jace = addReady(player1, new JaceBeleren());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(karn.getId(), jace.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(karn, jace);
        assertThat(vronos.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        while (!gd.stack.isEmpty()) {
            harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(karn, jace);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(karn, jace);
    }

    @Test
    void plusOneWithNoTargetsDoesNotPhaseOutVronos() {
        Permanent vronos = addReadyVronos(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vronos);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isNullOrEmpty();
    }

    @Test
    void minusTwoReturnsAtMostOneNonlandPermanentPerOpponent() {
        addReadyVronos(player1, 4);
        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent millstone = addReady(player2, new Millstone());
        Permanent land = addReady(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId(), millstone.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(millstone, land).doesNotContain(creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void minusSevenPermanentlyAnimatesTargetArtifact() {
        addReadyVronos(player1, 7);
        Permanent millstone = addReady(player1, new Millstone());

        harness.activateAbility(player1, 0, 2, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, millstone)).isTrue();
    }

    @Test
    void plusOneCreatesOneDelayedTriggerForBothPlaneswalkers() {
        addReadyVronos(player1, 4);
        Permanent karn = addReady(player1, new KarnScionOfUrza());
        Permanent jace = addReady(player1, new JaceBeleren());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(karn.getId(), jace.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(karn, jace);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneRejectsSelfOpposingPlaneswalkerAndNonPlaneswalker() {
        Permanent vronos = addReadyVronos(player1, 4);
        Permanent opposingVronos = addReady(player2, new VronosMaskedInquisitor());
        Permanent artifact = addReady(player1, new Millstone());

        for (Permanent illegalTarget : List.of(vronos, opposingVronos, artifact)) {
            assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                    player1, 0, 0, List.of(illegalTarget.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(vronos.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOnePhasesBackInOnlyDuringControllersUntapWithCountersIntact() {
        addReadyVronos(player1, 4);
        Permanent karn = addReady(player1, new KarnScionOfUrza());
        karn.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(karn.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(karn);
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(karn);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(karn);
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void minusTwoCanChooseNoTargets() {
        Permanent vronos = addReadyVronos(player1, 4);
        Permanent opposingArtifact = addReady(player2, new Millstone());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(vronos.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
    }

    @Test
    void minusTwoRejectsLandsAndOwnPermanents() {
        addReadyVronos(player1, 4);
        Permanent land = addReady(player2, new Island());
        Permanent ownArtifact = addReady(player1, new Millstone());

        for (Permanent illegalTarget : List.of(land, ownArtifact)) {
            assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                    player1, 0, 1, List.of(illegalTarget.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void minusSevenReplacesCreatureTypesAndPreservesAbilitiesAndCounters() {
        addReadyVronos(player1, 7);
        Permanent ornithopter = addReady(player1, new Ornithopter());
        ornithopter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 2, null, ornithopter.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gqs.hasEffectiveSubtype(gd, ornithopter, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ornithopter, CardSubtype.THOPTER)).isFalse();
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(11);
    }

    @Test
    void minusSevenRejectsOpposingArtifactsAndNonArtifacts() {
        addReadyVronos(player1, 7);
        Permanent opposingArtifact = addReady(player2, new Millstone());
        Permanent land = addReady(player1, new Island());

        for (Permanent illegalTarget : List.of(opposingArtifact, land)) {
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, illegalTarget.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void minusSevenAnimationAndGrantedAbilitiesSurviveTurnCleanupAndSourceLeaving() {
        Permanent vronos = addReadyVronos(player1, 7);
        Permanent millstone = addReady(player1, new Millstone());

        harness.activateAbility(player1, 0, 2, null, millstone.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vronos);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, millstone)).isTrue();
    }

    private Permanent addReadyVronos(Player player, int loyalty) {
        Permanent vronos = addReady(player, new VronosMaskedInquisitor());
        vronos.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return vronos;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        if (card.getLoyalty() != null) {
            permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
        }
        return permanent;
    }
}
