package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FootNinjas;
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

@CardUsed({RaphLeoSiblingRivals.class, FootNinjas.class})
class RaphLeoSiblingRivalsTest extends BaseCardTest {

    @Test
    @DisplayName("First attack untaps one or two attacking creatures and grants another combat")
    void firstAttackUntapsChosenAttackersAndGrantsExtraCombat() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(raphLeo.getId(), ninja.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(raphLeo.getId(), ninja.getId()));
        harness.passBothPriorities();

        assertThat(raphLeo.isTapped()).isFalse();
        assertThat(ninja.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("The target choice only offers attacking creatures")
    void targetChoiceOnlyOffersAttackingCreatures() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent nonAttacker = addCreatureReady(player1, new FootNinjas());

        declareAttackers(player1, List.of(0), 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(raphLeo.getId())
                .doesNotContain(nonAttacker.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(raphLeo.getId()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Attacking in a later combat phase does not grant another combat")
    void laterCombatAttackDoesNothing() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());

        declareAttackers(player1, List.of(0), 2);

        assertThat(gd.stack).isEmpty();
        assertThat(raphLeo.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing one attacker leaves the unchosen attacker tapped")
    void choosingOneTargetOnlyUntapsThatAttacker() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());

        declareAttackers(player1, List.of(0, 1), 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMultiplePermanentsChosen(player1, List.of(ninja.getId()));
            harness.passBothPriorities();
        });

        assertThat(ninja.isTapped()).isFalse();
        assertThat(raphLeo.isTapped()).isTrue();
        assertThat(ninja.isAttacking()).isTrue();
        assertThat(raphLeo.isAttacking()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target is skipped while the other target and extra combat still resolve")
    void oneTargetNoLongerAttackingDoesNotPreventExtraCombat() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());

        declareAttackers(player1, List.of(0, 1), 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMultiplePermanentsChosen(player1, List.of(raphLeo.getId(), ninja.getId()));
            assertThat(gd.stack).hasSize(1);
            ninja.setAttacking(false);
            harness.passBothPriorities();
        });

        assertThat(raphLeo.isTapped()).isFalse();
        assertThat(ninja.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("No extra combat is created when every chosen target becomes illegal")
    void allTargetsIllegalPreventsExtraCombat() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());

        declareAttackers(player1, List.of(0), 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMultiplePermanentsChosen(player1, List.of(raphLeo.getId()));
            assertThat(gd.stack).hasSize(1);
            raphLeo.setAttacking(false);
            harness.passBothPriorities();
        });

        assertThat(raphLeo.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves even if its source leaves the battlefield")
    void sourceLeavingDoesNotPreventUntapOrExtraCombat() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());

        declareAttackers(player1, List.of(0, 1), 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMultiplePermanentsChosen(player1, List.of(ninja.getId()));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(raphLeo);
            gd.playerGraveyards.get(player1.getId()).add(raphLeo.getCard());
            harness.passBothPriorities();
        });

        assertThat(ninja.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        gd.combatPhasesThisTurn = combatPhaseNumber;
        declareAttackers(player, attackerIndices);
    }
}
