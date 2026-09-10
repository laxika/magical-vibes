package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AboutFace;
import com.github.laxika.magicalvibes.cards.g.GhituFireEater;
import com.github.laxika.magicalvibes.cards.g.GraniteGrip;
import com.github.laxika.magicalvibes.cards.s.ShivanPhoenix;
import com.github.laxika.magicalvibes.cards.s.Snap;
import com.github.laxika.magicalvibes.cards.v.VigilantDrake;
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

@CardUsed({WeatherseedFaeries.class, GhituFireEater.class, ShivanPhoenix.class, VigilantDrake.class,
        AboutFace.class, Snap.class, GraniteGrip.class})
class WeatherseedFaeriesTest extends BaseCardTest {

    @Test
    @DisplayName("A red creature cannot block Weatherseed Faeries")
    void redCreatureCannotBlock() {
        Permanent faeries = addCreatureReady(player1, new WeatherseedFaeries());
        faeries.setAttacking(true);
        addCreatureReady(player2, new ShivanPhoenix());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A blue creature can block Weatherseed Faeries")
    void blueCreatureCanBlock() {
        Permanent faeries = addCreatureReady(player1, new WeatherseedFaeries());
        faeries.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VigilantDrake());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Weatherseed Faeries takes no combat damage from a red creature")
    void takesNoCombatDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player2, new GhituFireEater());
        attacker.setAttacking(true);
        Permanent faeries = addCreatureReady(player1, new WeatherseedFaeries());
        faeries.setBlocking(true);
        faeries.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(faeries.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Weatherseed Faeries takes combat damage from a blue creature")
    void takesCombatDamageFromBlueCreature() {
        Permanent attacker = addCreatureReady(player2, new VigilantDrake());
        attacker.setAttacking(true);
        Permanent faeries = addCreatureReady(player1, new WeatherseedFaeries());
        faeries.setBlocking(true);
        faeries.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Weatherseed Faeries");
        harness.assertInGraveyard(player1, "Weatherseed Faeries");
    }

    @Test
    @DisplayName("Weatherseed Faeries cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent faeries = addCreatureReady(player2, new WeatherseedFaeries());
        addCreatureReady(player2, new GhituFireEater());

        harness.setHand(player1, List.of(new AboutFace()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, faeries.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("A blue instant can target Weatherseed Faeries")
    void canBeTargetedByBlueInstant() {
        Permanent faeries = addCreatureReady(player2, new WeatherseedFaeries());

        harness.setHand(player1, List.of(new Snap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, faeries.getId());

        harness.assertNotOnBattlefield(player2, "Weatherseed Faeries");
        harness.assertInHand(player2, "Weatherseed Faeries");
    }

    @Test
    @DisplayName("A red Aura cannot enchant Weatherseed Faeries")
    void cannotBeEnchantedByRedAura() {
        Permanent faeries = addCreatureReady(player2, new WeatherseedFaeries());
        addCreatureReady(player2, new GhituFireEater());

        harness.setHand(player1, List.of(new GraniteGrip()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, faeries.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

}
