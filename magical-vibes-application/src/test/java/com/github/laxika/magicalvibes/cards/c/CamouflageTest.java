package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TwoHeadedGiantOfForiys;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Camouflage.class, GrizzlyBears.class, SerraAngel.class, TwoHeadedGiantOfForiys.class, RelentlessAssault.class})
class CamouflageTest extends BaseCardTest {

    @Test
    @DisplayName("assigns the only pile to the only attacker")
    void assignsPileToOnlyAttacker() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();
        harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId()));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).hasSize(1);
    }

    @Test
    @DisplayName("an empty pile leaves the attacker unblocked")
    void emptyPileLeavesAttackerUnblocked() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.isBlockedThisCombat()).isFalse();
    }

    @Test
    @DisplayName("can only be cast during declare attackers")
    void hasDeclareAttackersTimingRestriction() {
        harness.setHand(player1, List.of(new Camouflage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castCamouflage() {
        harness.castFromHand(player1, new Camouflage(), "{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("cannot be cast during an opponent's declare attackers step")
    void cannotBeCastDuringOpponentsDeclareAttackersStep() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        harness.setHand(player1, List.of(new Camouflage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("a ground creature in a flying attacker's pile does not block")
    void incompatibleBlockerDoesNotBlock() {
        addCreatureReady(player1, new SerraAngel());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId())));

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("a tapped creature may be placed in a pile but does not block")
    void tappedCreatureDoesNotBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setTapped(true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId())));

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("different piles are assigned to different attackers")
    void pilesAreAssignedToDifferentAttackers() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        castCamouflage();
        harness.handleMultiplePermanentsChosen(player2, List.of(firstBlocker.getId()));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player2, List.of(secondBlocker.getId())));

        assertThat(firstBlocker.getBlockingTargetIds()).hasSize(1);
        assertThat(secondBlocker.getBlockingTargetIds()).hasSize(1);
        assertThat(firstBlocker.getBlockingTargetIds()).doesNotContainAnyElementsOf(secondBlocker.getBlockingTargetIds());
        assertThat(List.of(firstBlocker.getBlockingTargetIds().getFirst(),
                secondBlocker.getBlockingTargetIds().getFirst()))
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
    }

    @Test
    @DisplayName("a creature with additional blocking capacity can be placed in two piles")
    void additionalBlockerCanAppearInTwoPiles() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new TwoHeadedGiantOfForiys());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        castCamouflage();
        harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId()));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId())));

        assertThat(blocker.getBlockingTargetIds())
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
    }

    @Test
    @CardUsed(RelentlessAssault.class)
    @DisplayName("continues to replace blocker declaration in an additional combat this turn")
    void appliesInAdditionalCombat() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castCamouflage();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player2, List.of()));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> harness.passBothPriorities());
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.passBothPriorities());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId())));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
