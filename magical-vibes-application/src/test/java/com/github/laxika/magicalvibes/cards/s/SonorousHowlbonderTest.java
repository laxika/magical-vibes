package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlleyStrangler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonorousHowlbonder.class, AlleyStrangler.class, GrizzlyBears.class})
class SonorousHowlbonderTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent creature = addReadyCreature(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private void advanceToBlockers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    void sonorousHowlbonderNeedsThreeBlockersItself() {
        addReadyAttacker(player1, new SonorousHowlbonder());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        advanceToBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    void grantsThreeBlockerRequirementToYourOtherMenaceCreatures() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyAttacker(player1, new AlleyStrangler());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        advanceToBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    void doesNotAffectYourCreaturesWithoutMenace() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyAttacker(player1, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    void doesNotAffectOpponentMenaceCreatures() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyAttacker(player2, new AlleyStrangler());

        advanceToBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
    }
}
