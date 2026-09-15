package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.l.LastBreath;
import com.github.laxika.magicalvibes.cards.m.MoltingHarpy;
import com.github.laxika.magicalvibes.cards.s.SnuffOut;
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
        NightwindGlider.class,
        FreshVolunteers.class,
        MoltingHarpy.class,
        DrakeHatchling.class,
        SnuffOut.class,
        LastBreath.class
})
class NightwindGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Nightwind Glider")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent glider = addCreatureReady(player1, new NightwindGlider());
        glider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, glider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A nonblack flying creature can block Nightwind Glider")
    void nonblackFlyingCreatureCanBlock() {
        Permanent glider = addCreatureReady(player1, new NightwindGlider());
        glider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DrakeHatchling());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, glider))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking Nightwind Glider")
    void protectionFromBlackPreventsBlocking() {
        Permanent glider = addCreatureReady(player1, new NightwindGlider());
        glider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MoltingHarpy());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, glider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new MoltingHarpy());
        attacker.setAttacking(true);
        Permanent glider = addCreatureReady(player2, new NightwindGlider());
        glider.setBlocking(true);
        glider.addBlockingTarget(indexOf(player1, attacker));

        resolveCombat(player1);

        assertThat(glider.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Nightwind Glider");
    }

    @Test
    @DisplayName("Protection from black prevents targeting Nightwind Glider")
    void protectionFromBlackPreventsTargeting() {
        Permanent glider = addCreatureReady(player2, new NightwindGlider());

        harness.setHand(player1, List.of(new SnuffOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, glider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("A white spell can target Nightwind Glider")
    void nonblackSpellCanTargetNightwindGlider() {
        Permanent glider = addCreatureReady(player2, new NightwindGlider());

        harness.setHand(player1, List.of(new LastBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, glider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nightwind Glider");
        harness.assertLife(player2, 24);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
