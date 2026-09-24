package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineGuard.class, BullHippo.class, GorillaWarrior.class})
class SanguineGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {1}{B} grants Sanguine Guard a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent guard = addCreatureReady(player1, new SanguineGuard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guard.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("A regeneration shield saves Sanguine Guard from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent guard = addCreatureReady(player1, new SanguineGuard());
        guard.setRegenerationShield(1);
        guard.setBlocking(true);
        guard.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new BullHippo());
        attacker.setAttacking(true);

        resolveCombat(player2);

        Permanent survivor = findPermanent(player1, "Sanguine Guard");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("First strike lets Sanguine Guard destroy a 3/2 blocker before it deals combat damage")
    void firstStrikeDealsDamageBeforeBlocker() {
        Permanent guard = addCreatureReady(player1, new SanguineGuard());
        guard.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player1, "Sanguine Guard");
        harness.assertNotOnBattlefield(player2, "Gorilla Warrior");
    }
}
