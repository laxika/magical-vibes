package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Greenseeker.class, Forest.class, Mountain.class, AshcoatBear.class})
class GreenseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        addReadyGreenseeker(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding a card searches a basic land into hand")
    void discardingSearchesBasicLandIntoHand() {
        addReadyGreenseeker(player1);
        Mountain discarded = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(chosen.getId());
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyGreenseeker(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Finishes without a choice when the library has no basic land")
    void noBasicLandCanBeFound() {
        addReadyGreenseeker(player1);
        Mountain discarded = new Mountain();
        AshcoatBear onlyLibraryCard = new AshcoatBear();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(onlyLibraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyLibraryCard);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanent(player1, "Greenseeker").isTapped()).isTrue();
    }

    private void addReadyGreenseeker(Player player) {
        addCreatureReady(player, new Greenseeker());
    }
}
