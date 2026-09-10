package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.cards.w.WallOfWonder;
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

@CardUsed({MaddeningImp.class, GrizzlyBears.class, WallOfAir.class})
class MaddeningImpTest extends BaseCardTest {

    /** player1 controls a ready Imp; it's player2's turn, in a step that precedes combat. */
    private Permanent primeImp() {
        Permanent imp = addCreatureReady(player1, new MaddeningImp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return imp;
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Forces the active player's creatures to attack this turn if able")
    void forcesActivePlayersCreaturesToAttack() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        primeImp();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("At end step, destroys non-Wall creatures that didn't attack, including summoning-sick creatures")
    void destroysNonAttackersAtEndStep() {
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttackedThisTurn(true);
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        Permanent summoningSick = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(summoningSick);

        primeImp();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(lazy, summoningSick)
                .contains(attacker, wall);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @CardUsed(WallOfWonder.class)
    @Test
    @DisplayName("Does not require a Wall to attack even when it can attack this turn")
    void doesNotRequireWallToAttack() {
        Permanent wall = addCreatureReady(player2, new WallOfWonder());
        primeImp();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(wall.isAttackedThisTurn()).isFalse();
    }

    @CardUsed(RagingGoblin.class)
    @Test
    @DisplayName("Requires a hasty non-Wall creature entering later in the turn to attack if able")
    void requiresLaterHastyCreatureToAttack() {
        primeImp();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated during your own turn")
    void cannotActivateOnYourOwnTurn() {
        addCreatureReady(player1, new MaddeningImp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's turn");
    }

    @Test
    @DisplayName("Cannot be activated once the combat phase has begun")
    void cannotActivateDuringCombat() {
        primeImp();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before combat");
    }
}
