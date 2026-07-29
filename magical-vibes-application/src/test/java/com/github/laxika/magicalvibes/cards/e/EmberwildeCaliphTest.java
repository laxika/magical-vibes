package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmberwildeCaliphTest extends BaseCardTest {

    private Permanent addAttacker(EmberwildeCaliph card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage
        harness.passBothPriorities(); // resolve the triggered ability
    }

    @Test
    @DisplayName("Combat damage to a player makes its controller lose that much life")
    void combatDamageToPlayerLosesLife() {
        addAttacker(new EmberwildeCaliph());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 4 combat damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16); // lost that much life
    }

    @Test
    @DisplayName("Loses no life when it deals no damage")
    void noLifeLossWithoutDamage() {
        EmberwildeCaliph caliph = new EmberwildeCaliph();
        caliph.setPower(0);
        addAttacker(caliph);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Must be declared as an attacker when able")
    void mustAttackWhenAble() {
        Permanent caliph = new Permanent(new EmberwildeCaliph());
        caliph.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(caliph);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
