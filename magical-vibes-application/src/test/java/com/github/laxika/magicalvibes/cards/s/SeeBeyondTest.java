package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeeBeyondTest extends BaseCardTest {

    @Test
    void drawsTwoCardsThenShufflesOneFromHandIntoLibrary() {
        Card shuffled = new Forest();
        Card kept = new Island();
        harness.setLibrary(player1, List.of(shuffled, kept, new Plains()));
        harness.setHand(player1, List.of(new SeeBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(shuffled.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(shuffled);
        harness.assertInGraveyard(player1, "See Beyond");
    }
}
