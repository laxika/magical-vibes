package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FastForward.class, GrizzlyBears.class})
class FastForwardTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each opponent attacked this turn")
    void reducedCostAfterAttackingOpponent() {
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(0));
        prepareMainPhase();

        harness.setHand(player1, List.of(new FastForward()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires its full cost when no opponent was attacked")
    void fullCostWithoutAttackingOpponent() {
        harness.setHand(player1, List.of(new FastForward()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Goads all creatures opponents control until the controller's next turn")
    void goadsOpposingCreatures() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FastForward()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Creatures entering after resolution are not goaded")
    void laterCreaturesAreNotGoaded() {
        castAndResolveFastForward();

        var laterCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.isGoaded(gd, laterCreature)).isFalse();
        declareAttackers(player2, List.of());
    }

    @Test
    @DisplayName("Only opposing creatures present at resolution are goaded")
    void excludesOwnCreaturesAndGoadsEveryOpposingCreature() {
        var ownCreature = addCreatureReady(player1, new GrizzlyBears());
        var firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        var secondOpponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAndResolveFastForward();

        assertThat(gqs.isGoaded(gd, ownCreature)).isFalse();
        assertThat(gqs.isGoaded(gd, firstOpponentCreature)).isTrue();
        assertThat(gqs.isGoaded(gd, secondOpponentCreature)).isTrue();
    }

    @Test
    @DisplayName("Goad remains on an affected creature if the caster gains control of it")
    void goadSurvivesControlChange() {
        var creature = addCreatureReady(player2, new GrizzlyBears());
        castAndResolveFastForward();

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        assertThat(gqs.isGoaded(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Goad lasts through the opponent's turn and expires at the caster's next turn")
    void goadExpiresAtNextTurn() {
        var creature = addCreatureReady(player2, new GrizzlyBears());
        castAndResolveFastForward();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isGoaded(gd, creature)).isTrue();
        declareAttackers(player2, List.of(0));
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isGoaded(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Multiple attacking creatures against one opponent give only one discount")
    void countsOpponentsRatherThanAttackers() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(0, 1));
        prepareMainPhase();
        harness.setHand(player1, List.of(new FastForward()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castAndResolveFastForward() {
        harness.setHand(player1, List.of(new FastForward()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
