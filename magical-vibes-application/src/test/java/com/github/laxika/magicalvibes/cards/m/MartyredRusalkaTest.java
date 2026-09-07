package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyredRusalka.class, GrizzlyBears.class, Forest.class})
class MartyredRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature stops the target creature from attacking this turn")
    void sacrificeCreatureLocksTargetFromAttacking() {
        addReadyRusalka(player1);
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The attack restriction wears off at end of turn")
    void attackRestrictionWearsOffAtEndOfTurn() {
        addReadyRusalka(player1);
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(target)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyRusalka(player1);
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyRusalka(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new MartyredRusalka());
    }

    private void declareAttack(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }
}
