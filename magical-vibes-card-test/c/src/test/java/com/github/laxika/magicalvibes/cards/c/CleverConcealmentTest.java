package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleverConcealment.class, Forest.class, GrizzlyBears.class, Millstone.class,
        Boomerang.class, Pacifism.class, SerraAngel.class})
class CleverConcealmentTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out any number of selected nonland permanents you control")
    void phasesOutSelectedNonlandPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(List.of(creature.getId(), artifact.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Allows choosing no permanents")
    void allowsChoosingNoPermanents() {
        castAndResolve(List.of());

        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land or a permanent controlled by an opponent")
    void cannotTargetIllegalPermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent you control");

        prepareCast();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent you control");
    }

    @Test
    @DisplayName("Phased-out permanents phase in during their controller's next untap")
    void phasesBackInDuringNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(List.of(creature.getId()));
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine permanents")
    void phasesOutOneHundredPermanents() {
        List<Permanent> creatures = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();

        castAndResolve(creatures.stream().map(Permanent::getId).toList());

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsAll(creatures);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(creatures);
    }

    @Test
    @DisplayName("Creatures with summoning sickness can convoke and then phase out")
    void convokingCreaturesCanAlsoBeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of(new CleverConcealment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        List<UUID> ids = List.of(first.getId(), second.getId());

        harness.castInstantWithConvoke(player1, 0, ids, ids);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(first, second);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("White creatures can convoke the colored cost and other creatures the generic cost")
    void convokeCanPayEntireManaCost() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new SerraAngel()),
                harness.addToBattlefieldAndReturn(player1, new SerraAngel()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleverConcealment()));
        List<UUID> ids = creatures.stream().map(Permanent::getId).toList();

        harness.castInstantWithConvoke(player1, 0, ids, ids);
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsAll(creatures);
        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("An opponent's attached Aura phases out and returns with its host")
    void opponentsAuraReturnsWithHostRatherThanOnOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());

        castAndResolve(List.of(creature.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(aura);
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(aura);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Targeting only an Aura leaves its host on the battlefield")
    void phasingOutAuraDoesNotPhaseOutHost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());

        castAndResolve(List.of(aura.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura).doesNotContain(creature);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An Aura and its host can both be targets and return still attached")
    void targetingAuraAndHostPhasesThemOutTogether() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());

        castAndResolve(List.of(aura.getId(), creature.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, aura);
        assertThat(aura.isPhasedOutIndirectly()).isTrue();
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("A remaining legal target phases out after another target is returned to hand")
    void resolvesForRemainingLegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        prepareCast();
        harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(artifact).doesNotContain(creature);
    }

    private void castAndResolve(List<UUID> targetIds) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new CleverConcealment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
