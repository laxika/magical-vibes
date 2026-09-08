package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildResearch.class, Pacifism.class, Shock.class, GrizzlyBears.class})
class WildResearchTest extends BaseCardTest {

    @Test
    @DisplayName("White ability searches for an enchantment, then discards it at random")
    void whiteAbilitySearchesForEnchantmentThenDiscards() {
        PendingInteraction.LibrarySearch search = activate(0, ManaColor.WHITE,
                List.of(new Pacifism(), new Shock(), new GrizzlyBears()));

        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Pacifism");
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.ENCHANTMENT));

        chooseFoundCard();

        harness.assertInGraveyard(player1, "Pacifism");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blue ability searches for an instant, then discards it at random")
    void blueAbilitySearchesForInstantThenDiscards() {
        PendingInteraction.LibrarySearch search = activate(1, ManaColor.BLUE,
                List.of(new Pacifism(), new Shock(), new GrizzlyBears()));

        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Shock");
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.INSTANT));

        chooseFoundCard();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private PendingInteraction.LibrarySearch activate(int abilityIndex, ManaColor coloredMana,
                                                       List<Card> library) {
        harness.addToBattlefield(player1, new WildResearch());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, coloredMana, 1);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseFoundCard() {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }
}
