package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MurmuringPhantasm;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlayedNimTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration ability creates a regeneration shield")
    void regenerationAbilityCreatesShield() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(nim.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage causes the damaged creature's controller to lose that much life")
    void combatDamageCausesLifeLossEqualToDamage() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID nimId = nim.getId();
        harness.castInstant(player1, 0, nimId);
        harness.passBothPriorities();

        nim.setAttacking(true);
        blockAttacker(player2, new MurmuringPhantasm(), 0);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The ability does not trigger from noncombat damage")
    void noncombatDamageDoesNotTriggerLifeLoss() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(nim.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent permanent = new Permanent(blockerCard);
        permanent.setSummoningSick(false);
        permanent.setBlocking(true);
        permanent.addBlockingTarget(attackerIndex);
        gd.playerBattlefields.get(blocker.getId()).add(permanent);
    }
}
