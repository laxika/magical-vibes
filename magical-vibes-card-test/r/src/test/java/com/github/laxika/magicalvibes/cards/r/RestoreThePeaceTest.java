package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@CardUsed({RestoreThePeace.class, GrizzlyBears.class, ProdigalSorcerer.class})
class RestoreThePeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creatures that dealt combat damage this turn, regardless of controller")
    void returnsCreaturesThatDealtCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownAttacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        harness.addToBattlefield(player2, new GrizzlyBears());

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
        gd.recordDamageDealtBySource(source.getId(), 2);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(damaged.getId());
    }

    @Test
    @DisplayName("Returns an attacker after actual combat damage to a player")
    void returnsAttackerAfterCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
        });

        cast();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner after it deals damage to a player")
    void returnsDamageDealerToOwner() {
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        gd.stolenCreatures.put(sorcerer.getId(), player2.getId());
        harness.activateAbility(player1, indexOf(player1, sorcerer), null, player2.getId());
        harness.passBothPriorities();

        cast();

        harness.assertNotOnBattlefield(player1, "Prodigal Sorcerer");
        harness.assertInHand(player2, "Prodigal Sorcerer");
        harness.assertNotInHand(player1, "Prodigal Sorcerer");
    }

    @Test
    @DisplayName("Does not return a creature whose damage was fully prevented")
    void leavesCreatureWhoseDamageWasPrevented() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        gd.preventAllDamageByCreatures = true;
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();

        cast();

        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
        harness.assertNotInHand(player2, "Prodigal Sorcerer");
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
