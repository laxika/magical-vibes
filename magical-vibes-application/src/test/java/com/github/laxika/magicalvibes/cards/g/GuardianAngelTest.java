package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianAngel.class, GrizzlyBears.class, LightningBolt.class})
class GuardianAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next X damage to a targeted player")
    void preventsInitialDamageToPlayer() {
        castGuardianAngel(2, player1.getId());

        castLightningBoltAtPlayer1();
        harness.assertLife(player1, 19);

        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("The controller may repeatedly pay {1} to add prevention to the same player")
    void repeatedPaymentAddsPrevention() {
        castGuardianAngel(1, player1.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.payGuardianAngel(player1, player1.getId());
        harness.payGuardianAngel(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(0);

        castLightningBoltAtPlayer1();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Prevents damage to a targeted creature")
    void preventsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGuardianAngel(2, creature.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The payment permission is tied to the original target and expires at cleanup")
    void permissionTargetAndDuration() {
        castGuardianAngel(0, player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.payGuardianAngel(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.payGuardianAngel(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.payGuardianAngel(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void paidPreventionExpiresAtCleanup() {
        castGuardianAngel(0, player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.payGuardianAngel(player1, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 17);
    }

    private void castGuardianAngel(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new GuardianAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castInstant(player1, 0, xValue, targetId);
        harness.passBothPriorities();
    }

    private void castLightningBoltAtPlayer1() {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }
}
