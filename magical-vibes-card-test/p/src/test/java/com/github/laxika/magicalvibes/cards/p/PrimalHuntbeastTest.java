package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.StaffOfNin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimalHuntbeast.class, Murder.class, StaffOfNin.class, PlanarCleansing.class})
class PrimalHuntbeastTest extends BaseCardTest {

    @Test
    void opponentCannotTargetWithSpell() {
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player1, new PrimalHuntbeast());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, huntbeast.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        harness.assertOnBattlefield(player1, "Primal Huntbeast");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetWithSpell() {
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player1, new PrimalHuntbeast());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, huntbeast.getId());

        harness.assertNotOnBattlefield(player1, "Primal Huntbeast");
        harness.assertInGraveyard(player1, "Primal Huntbeast");
        harness.assertInGraveyard(player1, "Murder");
    }

    @Test
    void opponentCannotTargetWithActivatedAbility() {
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player1, new PrimalHuntbeast());
        Permanent staff = harness.addToBattlefieldAndReturn(player2, new StaffOfNin());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, huntbeast.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(huntbeast.getMarkedDamage()).isZero();
        assertThat(staff.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetWithActivatedAbility() {
        harness.addToBattlefield(player1, new StaffOfNin());
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player1, new PrimalHuntbeast());

        harness.activateAbility(player1, 0, null, huntbeast.getId());
        harness.passBothPriorities();

        assertThat(huntbeast.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Primal Huntbeast");
    }

    @Test
    void hexproofDoesNotPreventOpponentsUntargetedRemoval() {
        harness.addToBattlefield(player1, new PrimalHuntbeast());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new PlanarCleansing()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertNotOnBattlefield(player1, "Primal Huntbeast");
        harness.assertInGraveyard(player1, "Primal Huntbeast");
    }
}
