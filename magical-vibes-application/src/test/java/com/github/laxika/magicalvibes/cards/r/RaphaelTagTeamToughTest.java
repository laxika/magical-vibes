package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaphaelTagTeamTough.class, GrizzlyBears.class})
class RaphaelTagTeamToughTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage untaps attacking creatures and creates an additional combat")
    void combatDamageUntapsAttackingCreaturesAndCreatesAdditionalCombat() {
        Permanent raphael = addCreatureReady(player1, new RaphaelTagTeamTough());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        gd.combatPhasesThisTurn = 1;

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        assertThat(raphael.isTapped()).isFalse();
        assertThat(bear.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Combat-damage ability triggers only once each turn")
    void combatDamageAbilityTriggersOnlyOnceEachTurn() {
        Permanent raphael = addCreatureReady(player1, new RaphaelTagTeamTough());
        gd.combatPhasesThisTurn = 1;

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(raphael.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Another creature dealing combat damage does not trigger Raphael")
    void anotherCreatureDealingCombatDamageDoesNotTriggerRaphael() {
        Permanent raphael = addCreatureReady(player1, new RaphaelTagTeamTough());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        raphael.tap();
        gd.combatPhasesThisTurn = 1;

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(raphael.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking Raphael")
    void menacePreventsSingleBlocker() {
        addCreatureReady(player1, new RaphaelTagTeamTough());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Damage to blockers does not create an additional combat")
    void damageToBlockersDoesNotCreateAdditionalCombat() {
        Permanent raphael = addCreatureReady(player1, new RaphaelTagTeamTough());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        gd.combatPhasesThisTurn = 1;

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(raphael.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("Regaining the ability after the first combat damage does not trigger it on later damage")
    void regainingAbilityAfterFirstDamageDoesNotTriggerOnLaterDamage() {
        Permanent raphael = addCreatureReady(player1, new RaphaelTagTeamTough());
        raphael.setLosesAllAbilitiesUntilEndOfTurn(true);
        gd.combatPhasesThisTurn = 1;
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(initialLife);
        assertThat(gd.additionalCombatPhasesOnly).isZero();

        raphael.setLosesAllAbilitiesUntilEndOfTurn(false);
        raphael.untap();
        gd.combatPhasesThisTurn = 2;
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(raphael.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }
}
