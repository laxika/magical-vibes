package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Bullwhip;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarriorEnKor.class, Shock.class, Bullwhip.class})
class WarriorEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects one damage from a two-damage event")
    void redirectsDamageToControlledCreature() {
        Permanent warrior = addCreatureReady(player1, new WarriorEnKor());
        Permanent destination = addCreatureReady(player1, new WarriorEnKor());

        harness.activateAbility(player1, indexOf(player1, warrior), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each activation redirects one damage")
    void multipleActivationsEachRedirectOneDamage() {
        Permanent warrior = addCreatureReady(player1, new WarriorEnKor());
        Permanent destination = addCreatureReady(player1, new WarriorEnKor());

        harness.activateAbility(player1, indexOf(player1, warrior), null, destination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, warrior), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The free ability redirects combat damage to a creature you control")
    void redirectsCombatDamage() {
        Permanent warrior = addCreatureReady(player1, new WarriorEnKor());
        Permanent destination = addCreatureReady(player1, new WarriorEnKor());
        Permanent attacker = addCreatureReady(player2, new WarriorEnKor());

        harness.activateAbility(player1, indexOf(player1, warrior), null, destination.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, warrior), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(warrior.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature as the destination")
    void cannotTargetOpponentsCreature() {
        Permanent warrior = addCreatureReady(player1, new WarriorEnKor());
        Permanent opponentCreature = addCreatureReady(player2, new WarriorEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, warrior), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void cannotTargetPlayer() {
        Permanent warrior = addCreatureReady(player1, new WarriorEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, warrior), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a controlled noncreature permanent")
    void cannotTargetControlledNoncreature() {
        Permanent warrior = addCreatureReady(player1, new WarriorEnKor());
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, warrior), null, bullwhip.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
