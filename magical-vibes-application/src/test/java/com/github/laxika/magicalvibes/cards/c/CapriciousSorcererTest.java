package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapriciousSorcerer.class, BogImp.class})
class CapriciousSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Capricious Sorcerer").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new BogImp());

        UUID targetId = harness.getPermanentId(player2, "Bog Imp");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bog Imp");
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupSorcererOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupSorcererOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new CapriciousSorcerer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot activate while it has summoning sickness")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CapriciousSorcerer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "DRAW"})
    void canActivateDuringBeginningPhase(TurnStep step) {
        setupSorcererOnMyTurn(step);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"DECLARE_BLOCKERS", "COMBAT_DAMAGE",
            "END_OF_COMBAT", "POSTCOMBAT_MAIN", "END_STEP"})
    void cannotActivateLaterInTurn(TurnStep step) {
        setupSorcererOnMyTurn(step);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
        assertThat(findPermanent(player1, "Capricious Sorcerer").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateBeforeAttackersInSecondCombat() {
        setupSorcererOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        findPermanent(player1, "Capricious Sorcerer").setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItsController() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void canTargetItself() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Capricious Sorcerer"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Capricious Sorcerer");
        harness.assertInGraveyard(player1, "Capricious Sorcerer");
    }

    private void setupSorcererOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new CapriciousSorcerer());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
