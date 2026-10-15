package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ShuFarmer.class)
class ShuFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when the ability resolves")
    void gainsLife() {
        setupFarmerOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Taps the farmer when the ability is activated")
    void tapsOnActivation() {
        setupFarmerOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Shu Farmer").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupFarmerOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once the declare attackers step begins")
    void cannotActivateAtDeclareAttackersStep() {
        setupFarmerOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void summoningSickCannotTap() {
        harness.addToBattlefield(player1, new ShuFarmer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new ShuFarmer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "DRAW"})
    void canActivateDuringBeginningPhase(TurnStep step) {
        setupFarmerOnMyTurn(step);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"DECLARE_BLOCKERS", "COMBAT_DAMAGE",
            "END_OF_COMBAT", "POSTCOMBAT_MAIN", "END_STEP"})
    void cannotActivateLaterInTurn(TurnStep step) {
        setupFarmerOnMyTurn(step);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");

        assertThat(findPermanent(player1, "Shu Farmer").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        setupFarmerOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 21);
    }

    @Test
    void abilityResolvesAfterFarmerLeavesBattlefield() {
        setupFarmerOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        var farmer = findPermanent(player1, "Shu Farmer");
        gd.playerBattlefields.get(player1.getId()).remove(farmer);
        gd.playerGraveyards.get(player1.getId()).add(farmer.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateBeforeAttackersInAnAdditionalCombat() {
        setupFarmerOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    private void setupFarmerOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new ShuFarmer());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
