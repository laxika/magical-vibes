package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

@CardUsed({AirElemental.class, GrizzlyBears.class, LightningBolt.class, Oasis.class})
class OasisTest extends BaseCardTest {

    private Permanent addOasis() {
        return harness.addToBattlefieldAndReturn(player1, new Oasis());
    }

    @Test
    @DisplayName("Tap ability adds 1 damage prevention shield to target creature")
    void preventsOnTargetCreature() {
        Permanent oasis = addOasis();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
        assertThat(oasis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addOasis();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Oasis());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addOasis();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    @Test
    @DisplayName("Prevents the next 1 noncombat damage dealt to the target creature")
    void preventsNextNoncombatDamageToTargetCreature() {
        addOasis();
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, elemental.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, elemental.getId());

        assertThat(elemental.getMarkedDamage()).isEqualTo(2);
        assertThat(elemental.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevention shield expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        addOasis();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isZero();
    }
}
