package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnworthyDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Unworthy Dead's regeneration ability creates a regeneration shield")
    void activationCreatesRegenerationShield() {
        addUnworthyDeadReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dead = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dead.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Unworthy Dead from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent dead = addUnworthyDeadReady(player1);
        dead.setRegenerationShield(1);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        EarthElemental attackerCard = new EarthElemental();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Unworthy Dead");
        Permanent survivingDead = findPermanent(player1, "Unworthy Dead");
        assertThat(survivingDead.isTapped()).isTrue();
        assertThat(survivingDead.getRegenerationShield()).isZero();
    }

    private Permanent addUnworthyDeadReady(Player player) {
        UnworthyDead card = new UnworthyDead();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
