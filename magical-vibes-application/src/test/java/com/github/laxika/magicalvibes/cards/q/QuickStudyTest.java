package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuickStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Quick Study puts it on the stack as an instant")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Resolving Quick Study draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }
}
