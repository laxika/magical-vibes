package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImpetuousSunchaserTest extends BaseCardTest {

    @Test
    @DisplayName("Impetuous Sunchaser must attack each combat if able")
    void mustAttackWhenAble() {
        addSunchaser(false);
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Impetuous Sunchaser can attack while summoning sick because it has haste")
    void hasteAllowsItToAttackImmediately() {
        addSunchaser(true);
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Impetuous Sunchaser while declaring another attacker is rejected")
    void mustBeIncludedAmongAttackers() {
        addSunchaser(false);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void addSunchaser(boolean summoningSick) {
        Permanent sunchaser = new Permanent(new ImpetuousSunchaser());
        sunchaser.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(player1.getId()).add(sunchaser);
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
