package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.e.Embolden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaintedPact.class, DuskImp.class, Embolden.class})
class TaintedPactTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first unique exiled card into hand and stops")
    void putsAcceptedCardIntoHandAndStops() {
        Card first = new DuskImp();
        Card second = new Embolden();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Continues exiling when a unique card is declined")
    void continuesAfterDecliningUniqueCard() {
        Card first = new DuskImp();
        Card second = new Embolden();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Dusk Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Stops after declining the last unique card")
    void stopsAfterDecliningLastUniqueCard() {
        Card only = new DuskImp();
        harness.setLibrary(player1, List.of(only));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Dusk Imp");
    }

    @Test
    @DisplayName("Stops on a duplicate name without offering it to hand")
    void stopsOnDuplicateName() {
        Card first = new DuskImp();
        Card duplicate = new DuskImp();
        Card remaining = new Embolden();
        harness.setLibrary(player1, List.of(first, duplicate, remaining));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Dusk Imp", "Dusk Imp");
    }

    @Test
    @DisplayName("Stops without prompting when the library is empty")
    void stopsWhenLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).isEmpty();
    }
}
