package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CustodianOfTheTrove.class)
class CustodianOfTheTroveTest extends BaseCardTest {

    @Test
    @DisplayName("Custodian of the Trove enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CustodianOfTheTrove()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Custodian of the Trove");
        Permanent custodian = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(custodian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Custodian of the Trove enters tapped even when it is not cast")
    void entersTappedWithoutBeingCast() {
        Permanent custodian = harness.enterBattlefieldAndReturn(player2, new CustodianOfTheTrove());

        harness.assertOnBattlefield(player2, "Custodian of the Trove");
        assertThat(custodian.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Custodian of the Trove untaps normally during its controller's untap step")
    void untapsNormally() {
        Permanent custodian = harness.enterBattlefieldAndReturn(player1, new CustodianOfTheTrove());
        assertThat(custodian.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(custodian.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Defender prevents an untapped Custodian of the Trove from attacking")
    void defenderPreventsAttacking() {
        Permanent custodian = harness.addToBattlefieldAndReturn(player1, new CustodianOfTheTrove());
        custodian.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
