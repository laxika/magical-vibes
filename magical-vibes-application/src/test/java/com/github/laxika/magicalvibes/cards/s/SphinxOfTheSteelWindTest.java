package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.e.EthercasteKnight;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfTheSteelWind.class, ShivanDragon.class, GiantSpider.class, WindDrake.class,
        CrawWurm.class, AirElemental.class, LightningBolt.class, GiantGrowth.class, Unsummon.class,
        EthercasteKnight.class, Pyroclasm.class, Oakenform.class})
class SphinxOfTheSteelWindTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Sphinx of the Steel Wind")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new SphinxOfTheSteelWind());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ShivanDragon());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature cannot block Sphinx of the Steel Wind")
    void greenCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new SphinxOfTheSteelWind());
        attacker.setAttacking(true);

        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Blue creature can block Sphinx of the Steel Wind")
    void blueCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new SphinxOfTheSteelWind());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new WindDrake());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sphinx takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new ShivanDragon());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SphinxOfTheSteelWind());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(attacker.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Sphinx of the Steel Wind");
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Sphinx of the Steel Wind");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Sphinx takes no combat damage from green creature")
    void takesNoDamageFromGreen() {
        Permanent attacker = addCreatureReady(player1, new CrawWurm());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SphinxOfTheSteelWind());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(attacker.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Sphinx of the Steel Wind");
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Sphinx of the Steel Wind");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Sphinx takes normal combat damage from blue creature")
    void takesNormalDamageFromBlue() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SphinxOfTheSteelWind());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(attacker.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Sphinx of the Steel Wind");
        harness.resolveCombatDamage();

        harness.assertNotOnBattlefield(player2, "Sphinx of the Steel Wind");
        harness.assertInGraveyard(player2, "Sphinx of the Steel Wind");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRed() {
        Permanent sphinx = addCreatureReady(player2, new SphinxOfTheSteelWind());

        // Add valid target so spell is playable
        addCreatureReady(player2, new WindDrake());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by green instant")
    void cannotBeTargetedByGreen() {
        Permanent sphinx = addCreatureReady(player2, new SphinxOfTheSteelWind());

        // Add valid target so spell is playable
        addCreatureReady(player2, new WindDrake());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by blue instant")
    void canBeTargetedByBlue() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheSteelWind());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, sphinx.getId());

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sphinx of the Steel Wind");
        harness.assertInHand(player1, "Sphinx of the Steel Wind");
    }

    @Test
    @DisplayName("A ground creature of an unprotected color cannot block the flying Sphinx")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new SphinxOfTheSteelWind()).setAttacking(true);
        addCreatureReady(player2, new EthercasteKnight());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Attacking with vigilance does not tap Sphinx")
    void attackingDoesNotTapSphinx() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheSteelWind());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(sphinx.isAttacking()).isTrue();
        assertThat(sphinx.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unblocked Sphinx deals first-strike damage and gains life only once")
    void unblockedFirstStrikeLifelink() {
        addCreatureReady(player1, new SphinxOfTheSteelWind()).setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("First strike kills a blue blocker before it deals damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheSteelWind());
        sphinx.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        harness.resolveCombatDamage();
        assertThat(sphinx.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Protection prevents damage from a nontargeted red spell")
    void preventsNontargetedRedDamage() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheSteelWind());
        addCreatureReady(player2, new WindDrake());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(sphinx.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sphinx of the Steel Wind");
        harness.assertInGraveyard(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Protection prevents targeting Sphinx with a green Aura")
    void cannotEnchantWithGreenAura() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheSteelWind());
        addCreatureReady(player1, new WindDrake());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
