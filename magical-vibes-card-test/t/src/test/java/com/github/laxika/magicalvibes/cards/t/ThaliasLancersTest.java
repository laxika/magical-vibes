package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GeierReachSanitarium;
import com.github.laxika.magicalvibes.cards.s.SteadfastCathar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThaliasLancers.class, ThaliaHereticCathar.class, SteadfastCathar.class, GeierReachSanitarium.class})
class ThaliasLancersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for a legendary card and put it into hand")
    void etbMaySearchForLegendaryCard() {
        harness.setHand(player1, List.of(new ThaliasLancers()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        Card legendaryCard = new ThaliaHereticCathar();
        harness.setLibrary(player1, List.of(legendaryCard, new SteadfastCathar()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(legendaryCard.getId());
        assertThat(search.params().cards()).allMatch(card -> card.getSupertypes().contains(CardSupertype.LEGENDARY));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(legendaryCard.getId());
    }

    @Test
    @DisplayName("ETB search has no legal choice when the library has no legendary card")
    void etbSearchFindsNoLegendaryCard() {
        harness.setHand(player1, List.of(new ThaliasLancers()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        harness.setLibrary(player1, List.of(new SteadfastCathar(), new SteadfastCathar()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void decliningSearchLeavesLibraryUntouched() {
        Card legendaryCard = new ThaliaHereticCathar();
        Card otherCard = new SteadfastCathar();
        harness.setLibrary(player1, List.of(legendaryCard, otherCard));
        castLancersAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(legendaryCard, otherCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canFindLegendaryLand() {
        Card legendaryLand = new GeierReachSanitarium();
        Card otherCard = new SteadfastCathar();
        harness.setLibrary(player1, List.of(otherCard, legendaryLand));
        castLancersAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(legendaryLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(text -> text.contains("reveals " + legendaryLand.getName())
                        && text.contains("into their hand"));
    }

    @Test
    void mayFailToFindEvenWhenLegendaryCardExists() {
        Card legendaryCard = new ThaliaHereticCathar();
        Card otherCard = new SteadfastCathar();
        harness.setLibrary(player1, List.of(legendaryCard, otherCard));
        castLancersAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(legendaryCard, otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchingEmptyLibraryCompletesAbility() {
        harness.setLibrary(player1, List.of());
        castLancersAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castLancersAndResolveTrigger() {
        harness.setHand(player1, List.of(new ThaliasLancers()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
