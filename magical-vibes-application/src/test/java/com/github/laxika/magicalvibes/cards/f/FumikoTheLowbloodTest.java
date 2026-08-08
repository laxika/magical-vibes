package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FumikoTheLowbloodTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido X scales to the number of attacking creatures when Fumiko becomes blocked")
    void bushidoScalesWithAttackersWhenBlocked() {
        Permanent fumiko = addReady(player1, new FumikoTheLowblood());
        Permanent ally = addReady(player1, new GrizzlyBears());
        fumiko.setAttacking(true);
        ally.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(fumiko.getPowerModifier()).isEqualTo(2);
        assertThat(fumiko.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido X scales to the number of attacking creatures when Fumiko blocks")
    void bushidoScalesWithAttackersWhenBlocking() {
        Permanent attacker1 = addReady(player1, new GrizzlyBears());
        Permanent attacker2 = addReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);
        Permanent fumiko = addReady(player2, new FumikoTheLowblood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(fumiko.getPowerModifier()).isEqualTo(2);
        assertThat(fumiko.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Unblocked Fumiko gets no Bushido bonus")
    void unblockedGetsNoBushido() {
        Permanent fumiko = addReady(player1, new FumikoTheLowblood());
        fumiko.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(fumiko.getPowerModifier()).isZero();
        assertThat(fumiko.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's creature must attack while Fumiko is on the battlefield")
    void opponentCreaturesMustAttack() {
        harness.addToBattlefield(player1, new FumikoTheLowblood());

        Permanent bears = addReady(player2, new GrizzlyBears());

        beginDeclareAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Fumiko's controller's creatures are not forced to attack by her static ability")
    void ownCreaturesNotForced() {
        harness.addToBattlefield(player1, new FumikoTheLowblood());
        Permanent bears = addReady(player1, new GrizzlyBears());

        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
