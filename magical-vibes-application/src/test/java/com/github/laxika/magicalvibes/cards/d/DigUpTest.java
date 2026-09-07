package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DigUp.class, Forest.class, GrizzlyBears.class})
class DigUpTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast searches for a basic land and puts it into hand")
    void normalCastSearchesForBasicLand() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        harness.setHand(player1, List.of(new DigUp()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Cleave cast searches for any card and puts it into hand")
    void cleaveCastSearchesForAnyCard() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        harness.setHand(player1, List.of(new DigUp()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(bears, forest);
        assertThat(search.params().reveals()).isFalse();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }
}
