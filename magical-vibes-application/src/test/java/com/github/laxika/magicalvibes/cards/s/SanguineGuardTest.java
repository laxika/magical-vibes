package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.z.ZhangFeiFierceWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanguineGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {1}{B} grants Sanguine Guard a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent guard = addReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guard.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Sanguine Guard from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent guard = addReady(player1);
        guard.setRegenerationShield(1);
        guard.setBlocking(true);
        guard.addBlockingTarget(0);

        Permanent attacker = new Permanent(new ZhangFeiFierceWarrior());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        Permanent survivor = findPermanent(player1, "Sanguine Guard");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
    }

    private Permanent addReady(Player player) {
        Permanent perm = new Permanent(new SanguineGuard());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
