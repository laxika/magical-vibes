package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelJiladExileTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addTelJiladExileReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent exile = findPermanent(player1, "Tel-Jilad Exile");
        assertThat(exile.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Tel-Jilad Exile from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent exile = addTelJiladExileReady(player1);
        exile.setRegenerationShield(1);
        exile.setBlocking(true);
        exile.addBlockingTarget(0);

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(3);
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tel-Jilad Exile");
        Permanent survivingExile = findPermanent(player1, "Tel-Jilad Exile");
        assertThat(survivingExile.isTapped()).isTrue();
        assertThat(survivingExile.getRegenerationShield()).isZero();
    }

    private Permanent addTelJiladExileReady(Player player) {
        Permanent permanent = new Permanent(new TelJiladExile());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
