package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Cloud Djinn can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent djinn = new Permanent(new CloudDjinn());
        djinn.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(djinn);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(djinn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Djinn cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        Permanent djinn = new Permanent(new CloudDjinn());
        djinn.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(djinn);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Cloud Djinn is unaffected when attacking")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent djinn = new Permanent(new CloudDjinn());
        djinn.setSummoningSick(false);
        djinn.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(djinn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
