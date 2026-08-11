package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScarredPumaTest extends BaseCardTest {

    private Permanent addReady(Permanent permanent) {
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void beginDeclareAttackers() {
        harness.addToBattlefield(player2, new GoblinSpy());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }

    @Test
    @DisplayName("Cannot attack without a black or green creature also attacking")
    void cannotAttackWithoutMatchingCreature() {
        Permanent puma = addReady(new Permanent(new ScarredPuma()));
        Permanent goblin = addReady(new Permanent(new GoblinSpy()));
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(puma.isAttacking()).isFalse();
        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with a black creature")
    void canAttackWithBlackCreature() {
        Permanent puma = addReady(new Permanent(new ScarredPuma()));
        Permanent blackCreature = addReady(new Permanent(new BlackKnight()));
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(puma.isAttacking()).isTrue();
        assertThat(blackCreature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can attack with a green creature")
    void canAttackWithGreenCreature() {
        Permanent puma = addReady(new Permanent(new ScarredPuma()));
        Permanent greenCreature = addReady(new Permanent(new LlanowarElves()));
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(puma.isAttacking()).isTrue();
        assertThat(greenCreature.isAttacking()).isTrue();
    }
}
