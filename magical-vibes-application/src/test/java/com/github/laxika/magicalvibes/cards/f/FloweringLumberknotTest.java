package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wingcrafter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloweringLumberknotTest extends BaseCardTest {

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player owner,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }

    private void pair(Permanent a, Permanent b) {
        a.setPairedWithId(b.getId());
        b.setPairedWithId(a.getId());
    }

    @Test
    @DisplayName("Unpaired Flowering Lumberknot can't attack")
    void unpairedCantAttack() {
        addReady(player1, new FloweringLumberknot());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paired Flowering Lumberknot can attack")
    void pairedCanAttack() {
        Permanent lumberknot = addReady(player1, new FloweringLumberknot());
        Permanent wingcrafter = addReady(player1, new Wingcrafter());
        pair(lumberknot, wingcrafter);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Unpaired Flowering Lumberknot can't block")
    void unpairedCantBlock() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new FloweringLumberknot());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paired Flowering Lumberknot can block")
    void pairedCanBlock() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent lumberknot = addReady(player2, new FloweringLumberknot());
        Permanent wingcrafter = addReady(player2, new Wingcrafter());
        pair(lumberknot, wingcrafter);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(lumberknot.isBlocking()).isTrue();
    }
}
