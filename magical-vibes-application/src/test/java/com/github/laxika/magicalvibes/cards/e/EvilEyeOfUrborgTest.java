package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvilEyeOfUrborg.class, GrizzlyBears.class})
class EvilEyeOfUrborgTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Eye creature you control cannot attack")
    void nonEyeCreatureCannotAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfUrborg());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Eye creature you control can attack")
    void eyeCreatureCanAttack() {
        addReadyCreature(player1, new EvilEyeOfUrborg());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("When Evil Eye becomes blocked, it destroys the blocker immediately when the trigger resolves")
    void becomesBlockedDestroysBlocker() {
        Permanent evilEye = addReadyCreature(player1, new EvilEyeOfUrborg());
        evilEye.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
