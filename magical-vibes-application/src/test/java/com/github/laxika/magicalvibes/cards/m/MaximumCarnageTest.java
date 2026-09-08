package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaximumCarnage.class, GrizzlyBears.class})
class MaximumCarnageTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I goads a creature controlled by the Saga's controller")
    void chapterIGoadsControllerCreature() {
        castAndResolveChapterI();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Chapter I goads opposing creatures that enter later")
    void chapterIGoadsLaterOpposingCreature() {
        castAndResolveChapterI();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Chapter I goad expires at the beginning of the controller's next turn")
    void chapterIGoadExpiresAtNextTurn() {
        castAndResolveChapterI();
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter II adds three red mana")
    void chapterIIAddsThreeRedMana() {
        addSagaWithLore(1);
        advanceToNextChapter();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter III deals five damage to each opponent")
    void chapterIIIDealsFiveDamageToOpponent() {
        addSagaWithLore(2);
        harness.setLife(player2, 20);
        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    private void castAndResolveChapterI() {
        harness.setHand(player1, List.of(new MaximumCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new MaximumCarnage());
        Permanent saga = findPermanent(player1, "Maximum Carnage");
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
