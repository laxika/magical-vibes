package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DwarvenScorcher;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({CommanderEesha.class, DwarvenScorcher.class, LavaDart.class, SuntailHawk.class})
class CommanderEeshaTest extends BaseCardTest {

    @Test
    @DisplayName("A creature cannot block Commander Eesha")
    void creatureCannotBlock() {
        Permanent eesha = addCreatureReady(player1, new CommanderEesha());
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player1, List.of(indexOf(player1, eesha)));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, eesha)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A creature ability cannot target Commander Eesha")
    void creatureAbilityCannotTargetEesha() {
        Permanent scorcher = addCreatureReady(player1, new DwarvenScorcher());
        Permanent eesha = addCreatureReady(player2, new CommanderEesha());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, scorcher), 0, null, eesha.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Creature combat damage to Commander Eesha is prevented")
    void creatureCombatDamageIsPrevented() {
        Permanent eesha = addCreatureReady(player1, new CommanderEesha());
        Permanent attacker = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player2, List.of(indexOf(player2, attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, eesha), indexOf(player2, attacker))));
        assertThat(eesha.isBlocking()).isTrue();

        harness.passBothPriorities();

        assertThat(eesha.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A noncreature spell can target Commander Eesha")
    void noncreatureSpellCanTargetEesha() {
        Permanent eesha = addCreatureReady(player2, new CommanderEesha());
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, eesha.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Noncreature spell damage can be dealt to Commander Eesha")
    void noncreatureSpellDamageCanBeDealtToEesha() {
        Permanent eesha = addCreatureReady(player2, new CommanderEesha());
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, eesha.getId());

        assertThat(eesha.getMarkedDamage()).isEqualTo(1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
