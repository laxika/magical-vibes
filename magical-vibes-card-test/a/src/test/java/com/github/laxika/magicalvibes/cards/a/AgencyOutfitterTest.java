package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.t.ThinkingCap;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgencyOutfitter.class, MagnifyingGlass.class, ThinkingCap.class, Forest.class, Island.class})
class AgencyOutfitterTest extends BaseCardTest {

    @Test
    @DisplayName("Without searching the library, both named cards are taken from the graveyard and hand")
    void findsNamedCardsInGraveyardAndHand() {
        MagnifyingGlass glass = new MagnifyingGlass();
        ThinkingCap cap = new ThinkingCap();
        harness.setGraveyard(player1, List.of(glass));
        harness.setHand(player1, List.of(new AgencyOutfitter(), cap));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultipleCardsChosen(player1, List.of(glass.getId(), cap.getId()));

        harness.assertOnBattlefield(player1, "Magnifying Glass");
        harness.assertOnBattlefield(player1, "Thinking Cap");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching the library finds both named cards there")
    void findsNamedCardsInLibrary() {
        MagnifyingGlass glass = new MagnifyingGlass();
        ThinkingCap cap = new ThinkingCap();
        harness.setLibrary(player1, List.of(glass, cap));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(glass.getId(), cap.getId()));

        assertThat(findPermanents(player1, "Magnifying Glass")).hasSize(1);
        assertThat(findPermanents(player1, "Thinking Cap")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningSearchDoesNothing() {
        harness.setGraveyard(player1, List.of(new MagnifyingGlass()));
        harness.setHand(player1, List.of(new AgencyOutfitter(), new ThinkingCap()));
        castAgencyOutfitter();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Magnifying Glass")).isEmpty();
        assertThat(findPermanents(player1, "Thinking Cap")).isEmpty();
        harness.assertInGraveyard(player1, "Magnifying Glass");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Thinking Cap");
    }

    @Test
    @DisplayName("At most one card of each name is taken")
    void takesOneCardOfEachName() {
        MagnifyingGlass first = new MagnifyingGlass();
        MagnifyingGlass second = new MagnifyingGlass();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(findPermanents(player1, "Magnifying Glass")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("The controller may find Thinking Cap without finding Magnifying Glass")
    void canFailToFindMagnifyingGlassAndFindThinkingCap() {
        ThinkingCap cap = new ThinkingCap();
        harness.setLibrary(player1, List.of(new MagnifyingGlass(), cap));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(cap.getId()));

        assertThat(findPermanents(player1, "Magnifying Glass")).isEmpty();
        harness.assertOnBattlefield(player1, "Thinking Cap");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Magnifying Glass");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A named card in hand may be left there")
    void handMatchMayBeLeftInHand() {
        MagnifyingGlass glass = new MagnifyingGlass();
        ThinkingCap cap = new ThinkingCap();
        harness.setGraveyard(player1, List.of(glass));
        harness.setHand(player1, List.of(new AgencyOutfitter(), cap));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultipleCardsChosen(player1, List.of(glass.getId()));

        harness.assertOnBattlefield(player1, "Magnifying Glass");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cap);
    }

    @Test
    @DisplayName("Not searching the library leaves it unsearched and in its order, even with a named card in it")
    void libraryLeftUnsearched() {
        ThinkingCap cap = new ThinkingCap();
        List<Card> library = List.of(new Forest(), cap, new Island(), new Forest(), new Island());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of(new MagnifyingGlass()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(findPermanents(player1, "Thinking Cap")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching with neither named card available completes without moving other cards")
    void noNamedCardsAvailable() {
        harness.setLibrary(player1, List.of(new AgencyOutfitter()));
        harness.setGraveyard(player1, List.of(new AgencyOutfitter()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Agency Outfitter")).hasSize(1);
        harness.assertInGraveyard(player1, "Agency Outfitter");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAgencyOutfitter() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTriggerAndAcceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.AndOrSearchZonesChoice.class);
    }
}
