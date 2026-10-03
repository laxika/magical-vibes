package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrogskolShieldmate.class, DauntlessCathar.class})
@DisplayName("Drogskol Shieldmate")
class DrogskolShieldmateTest extends BaseCardTest {

    private void castShieldmate() {
        harness.castFromHand(player1, new DrogskolShieldmate(), "{2}{W}");
    }

    @Test
    @DisplayName("ETB gives other creatures you control +0/+1 until end of turn")
    void etbBoostsOtherOwnCreatures() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new DauntlessCathar());

        castShieldmate();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB boost

        Permanent shieldmate = findPermanent(player1, "Drogskol Shieldmate");

        assertThat(cathar.getPowerModifier()).isEqualTo(0);
        assertThat(cathar.getToughnessModifier()).isEqualTo(1);
        assertThat(shieldmate.getPowerModifier()).isEqualTo(0);
        assertThat(shieldmate.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentCathar = harness.addToBattlefieldAndReturn(player2, new DauntlessCathar());

        castShieldmate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCathar.getPowerModifier()).isEqualTo(0);
        assertThat(opponentCathar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new DauntlessCathar());

        castShieldmate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cathar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new DauntlessCathar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        castShieldmate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drogskol Shieldmate");
        assertThat(cathar.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Other copies of Shieldmate receive the boost")
    void boostsAnotherShieldmateButNotItself() {
        Permanent earlierShieldmate = harness.addToBattlefieldAndReturn(player1, new DrogskolShieldmate());

        castShieldmate();
        harness.passBothPriorities();
        Permanent enteringShieldmate = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(earlierShieldmate.getId()))
                .findFirst().orElseThrow();
        harness.passBothPriorities();

        assertThat(earlierShieldmate.getToughnessModifier()).isEqualTo(1);
        assertThat(enteringShieldmate.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost applies to creatures present when the trigger resolves")
    void determinesAffectedCreaturesAtResolution() {
        castShieldmate();
        harness.passBothPriorities();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new DauntlessCathar());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new DauntlessCathar());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(0);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isEqualTo(0);
        assertThat(afterResolution.getToughnessModifier()).isEqualTo(0);
    }
}
