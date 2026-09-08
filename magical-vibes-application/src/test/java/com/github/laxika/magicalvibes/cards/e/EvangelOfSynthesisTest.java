package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvangelOfSynthesisTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing a card, then makes its controller discard a card")
    void entersDrawsThenDiscards() {
        GrizzlyBears discarded = new GrizzlyBears();
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

        harness.assertInGraveyard(player1, "Grizzly Bears");
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

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
