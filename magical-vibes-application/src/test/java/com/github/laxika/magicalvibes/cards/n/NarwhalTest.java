package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlibansTower;
import com.github.laxika.magicalvibes.cards.c.Chandler;
import com.github.laxika.magicalvibes.cards.l.LeapingLizard;
import com.github.laxika.magicalvibes.cards.r.RysorianBadger;
import com.github.laxika.magicalvibes.cards.s.Shrink;
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

@CardUsed({Narwhal.class, Chandler.class, LeapingLizard.class, AlibansTower.class,
        RysorianBadger.class, Shrink.class})
class NarwhalTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Narwhal")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new Narwhal());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Chandler());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Narwhal")
    void greenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new Narwhal());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LeapingLizard());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Narwhal takes no combat damage from a red attacker")
    void takesNoDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new Chandler());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Narwhal());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Narwhal");
    }

    @Test
    @DisplayName("Cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent attacker = addCreatureReady(player1, new LeapingLizard());
        attacker.setAttacking(true);

        Permanent narwhal = addCreatureReady(player2, new Narwhal());
        Permanent otherBlocker = addCreatureReady(player2, new LeapingLizard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.setHand(player1, List.of(new AlibansTower()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(narwhal.isBlocking()).isTrue();
        assertThat(otherBlocker.isBlocking()).isTrue();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, narwhal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by a non-red instant")
    void canBeTargetedByNonRedInstant() {
        Permanent narwhal = addCreatureReady(player1, new Narwhal());

        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, narwhal.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shrink");
    }

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it deals damage")
    void firstStrikeKillsBlockerFirst() {
        Permanent attacker = addCreatureReady(player1, new Narwhal());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new RysorianBadger());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Narwhal");
        harness.assertInGraveyard(player2, "Rysorian Badger");
    }
}
