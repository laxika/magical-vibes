package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvangelOfSynthesis.class, Forest.class})
class EvangelOfSynthesisTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing a card, then makes its controller discard a card")
    void entersDrawsThenDiscards() {
        EvangelOfSynthesis discarded = new EvangelOfSynthesis();
        Forest drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new EvangelOfSynthesis(), discarded)));
        harness.setLibrary(player1, List.of(drawn));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        int discardedIndex = gd.playerHands.get(player1.getId()).indexOf(discarded);
        harness.handleCardChosen(player1, discardedIndex);

        harness.assertInGraveyard(player1, "Evangel of Synthesis");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Gets +1/+0 and menace after its controller draws two cards this turn")
    void getsBonusAfterTwoDraws() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new EvangelOfSynthesis());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        drawCard();
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.MENACE)).isFalse();

        drawCard();
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not enable the bonus")
    void opponentDrawsDoNotEnableBonus() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new EvangelOfSynthesis());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Draws before entry count and discarding the drawn card keeps the bonus")
    void priorDrawAndEntryDrawEnableBonus() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        drawCard();

        Permanent evangel = harness.enterBattlefieldAndReturn(player1, new EvangelOfSynthesis());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(second));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("With an empty hand the entry trigger draws and discards the same card")
    void entryWithEmptyHandDiscardsDrawnCard() {
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new EvangelOfSynthesis());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Bonus applies beyond the second draw and ends when the next turn starts")
    void bonusEndsOnNextTurn() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new EvangelOfSynthesis());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        drawCard();
        drawCard();
        drawCard();

        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.MENACE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.MENACE)).isFalse();
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
