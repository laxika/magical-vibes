package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShepherdingSpirits.class, Plains.class, Forest.class, GrizzlyBears.class})
class ShepherdingSpiritsTest extends BaseCardTest {

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndSearchesForPlains() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shepherding Spirits");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Plainscycling puts the chosen Plains into hand")
    void plainscyclingChoosesPlains() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
    }
}
