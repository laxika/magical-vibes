package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MasterHealer.class, GrizzlyBears.class, Shock.class})
class MasterHealerTest extends BaseCardTest {

    private void addHealerReady() {
        addCreatureReady(player1, new MasterHealer());
    }

    @Test
    @DisplayName("Adds 4 prevention shield to target creature")
    void preventsOnCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("Adds 4 prevention shield to target player")
    void preventsOnPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Master Healer").isTapped()).isTrue();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Requires an any-target choice")
    void requiresTarget() {
        addHealerReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents only the next 4 damage to the targeted player")
    void preventsOnlyNextFourDamage() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        castShockAtPlayer2();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);

        castShockAtPlayer2();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();

        castShockAtPlayer2();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new MasterHealer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        addHealerReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent healer = findPermanent(player1, "Master Healer");
        healer.tap();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShockAtPlayer2() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
