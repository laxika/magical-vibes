package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.x.XathridDemon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaneslayerAngel.class, XathridDemon.class, ShivanDragon.class, SerraAngel.class,
        EnormousBaloth.class, GiantSpider.class, LightningBolt.class})
class BaneslayerAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Demon creature cannot block Baneslayer Angel")
    void demonCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        addCreatureReady(player2, new XathridDemon());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Dragon creature cannot block Baneslayer Angel")
    void dragonCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ShivanDragon());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Demon non-Dragon creature can block Baneslayer Angel")
    void regularCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Baneslayer Angel takes no combat damage from Demon creature")
    void takesNoDamageFromDemon() {
        Permanent attacker = addCreatureReady(player1, new XathridDemon());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BaneslayerAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5, player2.getId(), 2));

        harness.assertOnBattlefield(player1, "Xathrid Demon");
        harness.assertOnBattlefield(player2, "Baneslayer Angel");
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Baneslayer Angel takes no combat damage from a Dragon surviving first strike")
    void takesNoDamageFromDragon() {
        Permanent attacker = addCreatureReady(player1, new ShivanDragon());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BaneslayerAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Shivan Dragon");
        harness.assertOnBattlefield(player2, "Baneslayer Angel");
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Baneslayer Angel takes normal combat damage from non-Demon non-Dragon creature")
    void takesNormalDamageFromRegularCreature() {
        Permanent attacker = addCreatureReady(player1, new EnormousBaloth());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BaneslayerAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Enormous Baloth");
        harness.assertNotOnBattlefield(player2, "Baneslayer Angel");
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Baneslayer Angel")
    void groundCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        addCreatureReady(player2, new EnormousBaloth());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with reach can block Baneslayer Angel")
    void reachCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Baneslayer Angel deals damage once and gains that much life")
    void unblockedAttackGainsLife() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("First strike kills a flying blocker before it deals damage and gains full damage as life")
    void firstStrikeKillsBlockerAndGainsLife() {
        Permanent attacker = addCreatureReady(player1, new BaneslayerAngel());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Baneslayer Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection from Dragons does not stop a red spell without that subtype")
    void redSpellCanTargetAndDamageBaneslayer() {
        Permanent angel = addCreatureReady(player2, new BaneslayerAngel());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.assertOnBattlefield(player2, "Baneslayer Angel");
        assertThat(angel.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }
}
