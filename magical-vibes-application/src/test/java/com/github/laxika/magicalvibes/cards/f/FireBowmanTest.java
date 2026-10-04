package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireBowman.class, WeiInfantry.class, ChandraNalaar.class})
class FireBowmanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Fire Bowman");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 2/1")
    void deals1DamageDestroying1Toughness() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wei Infantry");
    }

    @Test
    @DisplayName("Deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Fire Bowman");
        harness.assertInGraveyard(player1, "Fire Bowman");
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during a later combat phase")
    void cannotActivateDuringLaterCombatPhase() {
        setupOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new FireBowman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can sacrifice a tapped creature with summoning sickness during upkeep")
    void canActivateWhileTappedDuringUpkeep() {
        setupOnMyTurn(TurnStep.UPKEEP);
        Permanent bowman = gd.playerBattlefields.get(player1.getId()).getFirst();
        bowman.setTapped(true);
        bowman.setSummoningSick(true);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Fire Bowman");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can target its controller")
    void canDamageController() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Fire Bowman");
    }

    @Test
    @DisplayName("Can target itself, but its sacrifice makes the target illegal on resolution")
    void selfTargetBecomesIllegalAfterSacrifice() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent bowman = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, bowman.getId());

        harness.assertInGraveyard(player1, "Fire Bowman");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bowman.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate during the postcombat main phase even without attackers")
    void cannotActivateInPostcombatMain() {
        setupOnMyTurn(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");

        harness.assertOnBattlefield(player1, "Fire Bowman");
        harness.assertNotInGraveyard(player1, "Fire Bowman");
        assertThat(gd.stack).isEmpty();
    }

    private void setupOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new FireBowman());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
