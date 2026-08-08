package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class RestoreThePeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creatures that dealt combat damage this turn, regardless of controller")
    void returnsCreaturesThatDealtCombatDamage() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        Permanent ownAttacker = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(ownAttacker);
        dealtCombatDamage(attacker, player1);
        dealtCombatDamage(ownAttacker, player2);

        cast();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves creatures that dealt no damage this turn on the battlefield")
    void leavesUndamagingCreatures() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        cast();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a creature that dealt noncombat damage to a creature this turn")
    void returnsNoncombatDamageDealer() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player2, indexOf(player2, sorcerer), null, bears.getId());
        harness.passBothPriorities();

        cast();

        harness.assertNotOnBattlefield(player2, "Prodigal Sorcerer");
        harness.assertInHand(player2, "Prodigal Sorcerer");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void dealtCombatDamage(Permanent source, Player damaged) {
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(damaged.getId());
    }

    private void cast() {
        harness.setHand(player1, List.of(new RestoreThePeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
