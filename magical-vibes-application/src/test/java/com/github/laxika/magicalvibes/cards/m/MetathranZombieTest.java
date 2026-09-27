package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetathranZombie.class, KavuTitan.class})
class MetathranZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration grants Metathran Zombie a regeneration shield")
    void activatingRegenerationGrantsShield() {
        Permanent zombie = addCreatureReady(player1, new MetathranZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
        assertThat(zombie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration cannot be activated with nonblack mana")
    void regenerationRequiresBlackMana() {
        Permanent zombie = addCreatureReady(player1, new MetathranZombie());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(zombie.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield saves Metathran Zombie from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent zombie = addCreatureReady(player1, new MetathranZombie());
        zombie.setRegenerationShield(1);
        zombie.setBlocking(true);
        zombie.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KavuTitan());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Metathran Zombie");
        assertThat(findPermanent(player1, "Metathran Zombie").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Metathran Zombie dies from lethal combat damage without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent zombie = addCreatureReady(player1, new MetathranZombie());
        zombie.setBlocking(true);
        zombie.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KavuTitan());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Metathran Zombie");
        harness.assertInGraveyard(player1, "Metathran Zombie");
    }
}
