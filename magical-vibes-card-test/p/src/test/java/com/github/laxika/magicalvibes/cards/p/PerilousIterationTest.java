package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerilousIteration.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class PerilousIterationTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks mana value 2 or less and mana value 3 or greater, then discards both next turn")
    void seeksBothManaValueRangesAndDiscardsThoseCards() {
        Card spell = new PerilousIteration();
        Card extra = new Forest();
        Card low = new GrizzlyBears();
        Card high = new HillGiant();
        harness.setHand(player1, List.of(spell, extra));
        harness.setLibrary(player1, List.of(low, high));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extra, low, high);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extra);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        spell.getId(), low.getId(), high.getId());
    }

    @Test
    @DisplayName("Seeks only the range that has a matching card")
    void resolvesEachSeekIndependentlyWhenOneRangeIsEmpty() {
        Card low = new GrizzlyBears();
        harness.setHand(player1, List.of(new PerilousIteration()));
        harness.setLibrary(player1, List.of(low));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(low);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(low.getId());
    }
}
