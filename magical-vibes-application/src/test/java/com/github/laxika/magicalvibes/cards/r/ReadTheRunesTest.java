package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReadTheRunes.class, Forest.class, GrizzlyBears.class})
class ReadTheRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Repeats the sacrifice-or-discard choice once for each card drawn")
    void repeatsChoiceForEachCardDrawn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReadTheRunes()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Repeats only for cards actually drawn when the library is short")
    void repeatsOnlyForCardsActuallyDrawn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ReadTheRunes()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
