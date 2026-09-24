package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
