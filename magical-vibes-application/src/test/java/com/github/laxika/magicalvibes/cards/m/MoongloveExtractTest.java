package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoongloveExtract.class, WoodlandChangeling.class, GoldmeadowHarrier.class, JaceBeleren.class})
class MoongloveExtractTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addReadyExtract(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating the ability sacrifices Moonglove Extract to the graveyard")
    void activatingSacrificesToGraveyard() {
        addReadyExtract(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Moonglove Extract");
        harness.assertInGraveyard(player1, "Moonglove Extract");
    }

    @Test
    @DisplayName("Deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyExtract(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 2/2")
    void deals2DamageDestroying2Toughness() {
        addReadyExtract(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 1/1")
    void deals2DamageKilling1Toughness() {
        addReadyExtract(player1);
        harness.addToBattlefield(player2, new GoldmeadowHarrier());

        UUID targetId = harness.getPermanentId(player2, "Goldmeadow Harrier");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goldmeadow Harrier");
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        addReadyExtract(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.activateAbility(player1, 0, null, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker after the source is sacrificed")
    void deals2DamageToPlaneswalker() {
        addReadyExtract(player1);
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());

        harness.activateAbility(player1, 0, null, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player1, "Moonglove Extract");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped, newly entered Extract can activate without mana")
    void tappedExtractCanActivateWithoutMana() {
        Permanent extract = addReadyExtract(player1);
        extract.tap();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Moonglove Extract");
        harness.assertInGraveyard(player1, "Moonglove Extract");
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        addReadyExtract(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Moonglove Extract");
    }

    private Permanent addReadyExtract(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MoongloveExtract());
    }
}
