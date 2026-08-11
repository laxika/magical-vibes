package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrborgDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring no attackers while Urborg Drake can attack throws exception")
    void mustAttackWhenAble() {
        addDrake(false);
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Urborg Drake while declaring another attacker throws exception")
    void mustBeIncludedAmongAttackers() {
        addDrake(false);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Urborg Drake does not have to attack while summoning sick")
    void doesNotAttackWithSummoningSickness() {
        addDrake(true);

        beginAttackers();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isFalse();
    }

    private void addDrake(boolean summoningSick) {
        Permanent drake = new Permanent(new UrborgDrake());
        drake.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(player1.getId()).add(drake);
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
