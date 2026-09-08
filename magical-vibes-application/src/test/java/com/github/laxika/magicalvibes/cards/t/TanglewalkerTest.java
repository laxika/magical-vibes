package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.model.Card;
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

class TanglewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tanglewalker makes itself unblockable while defending player controls an artifact land")
    void makesItselfUnblockableWithArtifactLand() {
        harness.addToBattlefield(player2, new SeatOfTheSynod());
        Permanent blocker = addCreature(player2);
        Permanent tanglewalker = addAttackingCreature(player1, new Tanglewalker());

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker),
                        battlefieldIndex(player1, tanglewalker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Tanglewalker affects each creature you control")
    void makesOtherCreaturesYouControlUnblockableWithArtifactLand() {
        harness.addToBattlefield(player2, new SeatOfTheSynod());
        Permanent blocker = addCreature(player2);
        Permanent bears = addAttackingCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Tanglewalker());

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker),
                        battlefieldIndex(player1, bears)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("An artifact that is not a land does not enable Tanglewalker's ability")
    void artifactNonlandDoesNotEnableAbility() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent blocker = addCreature(player2);
        addAttackingCreature(player1, new Tanglewalker());

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tanglewalker does not affect creatures controlled by an opponent")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new SeatOfTheSynod());
        harness.addToBattlefield(player1, new Tanglewalker());
        Permanent blocker = addCreature(player1);
        Permanent opponentCreature = addAttackingCreature(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(battlefieldIndex(player1, blocker),
                        battlefieldIndex(player2, opponentCreature))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
