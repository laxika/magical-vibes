package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetathranZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration grants Metathran Zombie a regeneration shield")
    void activatingRegenerationGrantsShield() {
        addZombieReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Metathran Zombie").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Metathran Zombie from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent zombie = addZombieReady(player1);
        zombie.setRegenerationShield(1);
        zombie.setBlocking(true);
        zombie.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 2, 2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Metathran Zombie");
        assertThat(findPermanent(player1, "Metathran Zombie").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Metathran Zombie dies from lethal combat damage without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent zombie = addZombieReady(player1);
        zombie.setBlocking(true);
        zombie.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 2, 2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Metathran Zombie");
        harness.assertInGraveyard(player1, "Metathran Zombie");
    }

    private Permanent addZombieReady(Player player) {
        Permanent perm = new Permanent(new MetathranZombie());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
