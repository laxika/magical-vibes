package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FlailingManticore;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.l.LastBreath;
import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.cards.l.LightningHounds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ThermalGlider.class,
        FlailingManticore.class,
        FreshVolunteers.class,
        Lunge.class,
        LastBreath.class,
        LightningHounds.class
})
class ThermalGliderTest extends BaseCardTest {

    @Test
    @DisplayName("A red flying creature cannot block Thermal Glider")
    void redCreatureCannotBlock() {
        Permanent glider = addCreatureReady(player1, new ThermalGlider());
        glider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FlailingManticore());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(glider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Thermal Glider")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent glider = addCreatureReady(player1, new ThermalGlider());
        glider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(glider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Thermal Glider cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent glider = addCreatureReady(player2, new ThermalGlider());

        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(glider.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Thermal Glider takes no combat damage from a red creature")
    void takesNoCombatDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player2, new LightningHounds());
        attacker.setAttacking(true);
        Permanent glider = addCreatureReady(player1, new ThermalGlider());
        glider.setBlocking(true);
        glider.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(glider.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Thermal Glider");
        harness.assertNotOnBattlefield(player2, "Lightning Hounds");
    }

    @Test
    @DisplayName("A white instant can target Thermal Glider")
    void nonRedInstantCanTargetThermalGlider() {
        Permanent glider = addCreatureReady(player2, new ThermalGlider());

        harness.setHand(player1, List.of(new LastBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, glider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Thermal Glider");
        harness.assertLife(player2, 24);
    }
}
