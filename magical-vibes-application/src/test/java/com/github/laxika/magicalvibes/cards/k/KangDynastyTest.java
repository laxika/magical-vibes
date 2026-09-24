package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KangDynasty.class, Forest.class, GrizzlyBears.class})
class KangDynastyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I taps and goads one opponent creature and draws for its combat damage")
    void chapterITapsGoadsAndWatchesCreature() {
        Permanent saga = addSagaWithLore(0);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSizeBeforeChapter = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeChapter);

        opponentCreature.untap();
        opponentCreature.setAttacking(true);
        int handSizeBeforeCombat = gd.playerHands.get(player1.getId()).size();
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCombat + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Chapter III gives a creature a hand-size boost and makes it unblockable")
    void chapterIIIBoostsAndMakesCreatureUnblockable() {
        addSagaWithLore(2);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new KangDynasty());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Kang Dynasty"))
                .findFirst().orElseThrow();
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
