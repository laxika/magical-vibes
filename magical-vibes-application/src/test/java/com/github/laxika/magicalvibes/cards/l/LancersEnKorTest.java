package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
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

@CardUsed({LancersEnKor.class, Shock.class, HonorGuard.class})
class LancersEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects damage to a creature you control")
    void redirectsDamageToControlledCreature() {
        Permanent lancers = addCreatureReady(player1, new LancersEnKor());
        Permanent destination = addCreatureReady(player1, new LancersEnKor());
        Permanent attacker = addCreatureReady(player2, new HonorGuard());

        harness.activateAbility(player1, indexOf(player1, lancers), null, destination.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, lancers), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(lancers.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only one damage from a two-damage event is redirected")
    void redirectsOnlyOneDamageFromLargerEvent() {
        Permanent lancers = addCreatureReady(player1, new LancersEnKor());
        Permanent destination = addCreatureReady(player1, new LancersEnKor());

        harness.activateAbility(player1, indexOf(player1, lancers), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, lancers.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(lancers.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent lancers = addCreatureReady(player1, new LancersEnKor());
        Permanent opponentCreature = addCreatureReady(player2, new LancersEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, lancers), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void cannotTargetPlayer() {
        Permanent lancers = addCreatureReady(player1, new LancersEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, lancers), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
