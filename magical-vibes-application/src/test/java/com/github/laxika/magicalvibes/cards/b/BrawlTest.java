package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrawlTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain the power damage ability until end of turn")
    void allCreaturesGainPowerDamageAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent ownTarget = addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingTarget = addCreatureReady(player2, new SerraAngel());

        castBrawl();

        harness.activateAbility(player1, 0, null, opposingTarget.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, ownTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingTarget.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that enter after Brawl resolves do not gain the ability")
    void laterCreaturesDoNotGainAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        castBrawl();

        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new SerraAngel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(laterCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        castBrawl();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability can target only creatures")
    void grantedAbilityCannotTargetPlayer() {
        addCreatureReady(player1, new GrizzlyBears());
        castBrawl();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBrawl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Brawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
