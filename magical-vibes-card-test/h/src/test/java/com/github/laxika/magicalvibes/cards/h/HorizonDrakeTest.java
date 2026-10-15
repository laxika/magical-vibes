package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CelestialColonnade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SmolderingSpires;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HorizonDrake.class, Forest.class, GrizzlyBears.class, CelestialColonnade.class,
        SmolderingSpires.class})
class HorizonDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Horizon Drake has protection from lands")
    void hasProtectionFromLands() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new HorizonDrake());
        Permanent land = new Permanent(new Forest());
        Permanent creature = new Permanent(new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, drake, land)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, drake, creature)).isFalse();
    }

    @Test
    void flyingLandCreatureCannotBlockDrake() {
        addCreatureReady(player1, new HorizonDrake()).setAttacking(true);
        Permanent colonnade = addCreatureReady(player2, new CelestialColonnade());
        animateColonnade(player2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(colonnade.isBlocking()).isFalse();
    }

    @Test
    void drakeCanBlockLandCreatureAndPreventsItsCombatDamage() {
        Permanent colonnade = addCreatureReady(player1, new CelestialColonnade());
        animateColonnade(player1);
        Permanent drake = addCreatureReady(player2, new HorizonDrake());
        colonnade.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Horizon Drake");
        harness.assertOnBattlefield(player1, "Celestial Colonnade");
        assertThat(drake.getMarkedDamage()).isZero();
        assertThat(colonnade.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void groundCreatureCannotBlockFlyingDrake() {
        addCreatureReady(player1, new HorizonDrake()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void landEntryAbilityCannotTargetDrake() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new HorizonDrake());
        harness.setHand(player1, List.of(new SmolderingSpires()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(drake.isCantBlockThisTurn()).isFalse();
    }

    private void animateColonnade(Player controller) {
        harness.addMana(controller, ManaColor.COLORLESS, 3);
        harness.addMana(controller, ManaColor.WHITE, 1);
        harness.addMana(controller, ManaColor.BLUE, 1);
        harness.activateAbility(controller, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
