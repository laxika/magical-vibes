package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XunYuWeiAdvisor.class, WeiInfantry.class, Swamp.class})
class XunYuWeiAdvisorTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gets +2/+0 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Wei Infantry");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent infantry = findPermanent(player1, "Wei Infantry");
        assertThat(infantry.getPowerModifier()).isEqualTo(2);
        assertThat(infantry.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Wei Infantry");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent infantry = findPermanent(player1, "Wei Infantry");
        assertThat(infantry.getPowerModifier()).isEqualTo(0);
        assertThat(infantry.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps Xun Yu when the ability is activated")
    void tapsOnActivation() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Wei Infantry");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Xun Yu, Wei Advisor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        UUID targetId = harness.getPermanentId(player1, "Wei Infantry");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new XunYuWeiAdvisor());
        addCreatureReady(player1, new WeiInfantry());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Wei Infantry");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new WeiInfantry());
        UUID opponentTargetId = harness.getPermanentId(player2, "Wei Infantry");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Swamp());
        UUID landTargetId = harness.getPermanentId(player1, "Swamp");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, landTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new XunYuWeiAdvisor());
        addCreatureReady(player1, new WeiInfantry());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
