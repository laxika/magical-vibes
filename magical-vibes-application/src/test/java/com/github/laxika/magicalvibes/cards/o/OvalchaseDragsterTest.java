package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvalchaseDragster.class, GrizzlyBears.class, DukharaPeafowl.class})
class OvalchaseDragsterTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent dragster = addDragsterReady(player1);

        assertThat(gqs.isCreature(gd, dragster)).isFalse();
    }

    @Test
    void crewAnimatesDragsterAndTapsCrew() {
        Permanent dragster = addDragsterReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dragster)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dragster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dragster)).isEqualTo(1);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addDragsterReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent dragster = addDragsterReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, dragster)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dragster)).isFalse();
    }

    private Permanent addDragsterReady(Player player) {
        return addCreatureReady(player, new OvalchaseDragster());
    }

    @Test
    void crewTapsCreatureAsCostButAnimatesOnlyOnResolution() {
        Permanent dragster = addDragsterReady(player1);
        Permanent crew = addCreatureReady(player1, new DukharaPeafowl());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(dragster.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, dragster)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dragster)).isTrue();
    }

    @Test
    void newlyCastDragsterCanBeCrewedBySummoningSickCreatureAndAttack() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new OvalchaseDragster(), "{4}");
        resolveAllTriggers();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(crew.isTapped()).isTrue();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void trampleDealsDamageBeyondBlockersToughness() {
        harness.setLife(player2, 20);
        addDragsterReady(player1);
        addCreatureReady(player1, new DukharaPeafowl());
        Permanent blocker = addCreatureReady(player2, new DukharaPeafowl());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Dukhara Peafowl");
        harness.assertInGraveyard(player1, "Ovalchase Dragster");
    }

    @Test
    void tappedAndOpposingCreaturesCannotCrew() {
        addDragsterReady(player1);
        Permanent tappedCrew = addCreatureReady(player1, new DukharaPeafowl());
        tappedCrew.tap();
        Permanent opposingCrew = addCreatureReady(player2, new DukharaPeafowl());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(opposingCrew.isTapped()).isFalse();
    }

    @Test
    void animatedDragsterCannotCrewItself() {
        Permanent dragster = addDragsterReady(player1);
        addCreatureReady(player1, new DukharaPeafowl());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(dragster.isTapped()).isFalse();
    }

    @Test
    void canTapMoreCreaturesThanNeededForOneCrewActivation() {
        Permanent dragster = addDragsterReady(player1);
        Permanent first = addCreatureReady(player1, new DukharaPeafowl());
        Permanent second = addCreatureReady(player1, new DukharaPeafowl());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, dragster)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, dragster)).isTrue();
    }

    @Test
    void animationPersistsThroughEndStepAndEndsDuringCleanup() {
        Permanent dragster = addDragsterReady(player1);
        addCreatureReady(player1, new DukharaPeafowl());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, dragster)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, dragster)).isFalse();
    }
}
