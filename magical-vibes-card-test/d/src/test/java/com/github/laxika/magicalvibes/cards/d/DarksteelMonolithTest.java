package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelMonolith.class, GrizzlyBears.class, MindStone.class, ScourFromExistence.class,
        WalkingBallista.class})
class DarksteelMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a colorless spell from hand for free")
    void castsColorlessSpellFromHandForFree() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce the cost of a colored spell")
    void doesNotReduceColoredSpellCost() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be used only once each turn")
    void canBeUsedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone(), new MindStone()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be used during an opponent's turn")
    void canBeUsedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Darksteel Monolith"));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent cannot use Monolith's alternative cost")
    void opponentCannotUseAlternativeCost() {
        harness.addToBattlefield(player2, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Monolith supplies its own free cast each turn")
    void eachMonolithSuppliesIndependentFreeCast() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone(), new MindStone(), new MindStone()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The free cast resets on the opponent's next turn")
    void freeCastResetsOnNextTurn() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone(), new ScourFromExistence()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Mind Stone"));

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A spell cast for zero cannot choose a positive X")
    void cannotChoosePositiveXWithAlternativeCost() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new WalkingBallista()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
