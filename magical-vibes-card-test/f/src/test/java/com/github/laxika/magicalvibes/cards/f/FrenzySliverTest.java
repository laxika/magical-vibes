package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.h.HomingSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenzySliver.class, HomingSliver.class, BlindPhantasm.class, Ghostfire.class})
class FrenzySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Frenzy Sliver gives an unblocked Sliver +1/+0 until end of turn")
    void unblockedSliverGetsBoost() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(frenzySliver.getPowerModifier()).isEqualTo(1);
        assertThat(frenzySliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Frenzy Sliver gives another player's unblocked Sliver +1/+0")
    void grantsFrenzyToOpposingSliver() {
        addCreatureReady(player1, new FrenzySliver());
        Permanent opposingSliver = addCreatureReady(player2, new HomingSliver());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(opposingSliver.getPowerModifier()).isEqualTo(1);
        assertThat(opposingSliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocked Sliver does not get a frenzy boost")
    void blockedSliverGetsNoBoost() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(frenzySliver.getPowerModifier()).isZero();
        assertThat(frenzySliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An unblocked Sliver keeps its frenzy trigger if the granting Sliver dies")
    void unblockedSliverKeepsFrenzyAfterGrantingSliverDies() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        Permanent unblockedSliver = addCreatureReady(player1, new HomingSliver());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.castAndResolveInstant(player1, 0, frenzySliver.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Frenzy Sliver");
        assertThat(unblockedSliver.getPowerModifier()).isEqualTo(1);
        assertThat(unblockedSliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple instances of frenzy trigger separately")
    void multipleFrenzyInstancesTriggerSeparately() {
        addCreatureReady(player1, new FrenzySliver());
        addCreatureReady(player1, new FrenzySliver());
        Permanent attacker = addCreatureReady(player1, new HomingSliver());

        declareAttackersAndPrepareBlockers(List.of(2));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.passBothPriorities());
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing the granting Sliver before blockers prevents frenzy from triggering")
    void removingGrantBeforeBlockersPreventsFrenzy() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        Permanent attacker = addCreatureReady(player1, new HomingSliver());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.castAndResolveInstant(player1, 0, frenzySliver.getId());
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Frenzy Sliver");
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Frenzy Sliver does not grant the ability to non-Slivers")
    void nonSliverGetsNoBoost() {
        addCreatureReady(player1, new FrenzySliver());
        Permanent nonSliver = addCreatureReady(player1, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(nonSliver.getPowerModifier()).isZero();
        assertThat(nonSliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Frenzy's temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(frenzySliver.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(frenzySliver.getPowerModifier()).isZero();
        assertThat(frenzySliver.getToughnessModifier()).isZero();
    }
}
