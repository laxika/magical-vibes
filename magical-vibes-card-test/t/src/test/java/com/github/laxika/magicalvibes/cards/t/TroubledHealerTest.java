package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TroubledHealer.class, Forest.class, LlanowarElves.class, MoggFanatic.class})
class TroubledHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a land and prevents the next 2 damage to a player")
    void sacrificesLandAndPreventsNextTwoDamageToPlayer() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Forest");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player2, 0, null, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents damage to a target creature")
    void preventsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new MoggFanatic());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new TroubledHealer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Repeated activations add shields without tapping the healer")
    void repeatedActivationsPreventFourDamage() {
        harness.addToBattlefield(player1, new TroubledHealer());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new MoggFanatic());
        }
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new Forest());
            harness.activateAbility(player1, 0, null, player1.getId());
            harness.passBothPriorities();
        }

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player2, 0, null, player1.getId());
            harness.passBothPriorities();
            harness.assertLife(player1, i < 4 ? 20 : 19);
        }
        harness.assertOnBattlefield(player1, "Troubled Healer");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("An ordinary land is not a legal damage-prevention target")
    void cannotTargetOrdinaryLand() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player1, new Forest());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }
}
