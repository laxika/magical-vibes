package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneInfusion.class, Divination.class, GrizzlyBears.class, Island.class, Shock.class, Swamp.class})
class ArcaneInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Only instant and sorcery cards among the top four are offered")
    void offersOnlyInstantsAndSorceries() {
        Card shock = new Shock();
        Card divination = new Divination();
        setupTopCards(List.of(shock, new GrizzlyBears(), divination, new Island()));
        cast();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), divination.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The chosen spell goes to hand and the rest go to the bottom")
    void chosenSpellGoesToHandAndRestToBottom() {
        Card shock = new Shock();
        setupTopCards(List.of(shock, new GrizzlyBears(), new Divination(), new Island()));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        harness.assertInHand(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Divination", "Island");
        harness.assertInGraveyard(player1, "Arcane Infusion");
    }

    @Test
    @DisplayName("Declining the reveal puts all four cards on the bottom")
    void mayDecline() {
        setupTopCards(List.of(new Shock(), new GrizzlyBears(), new Divination(), new Island()));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Grizzly Bears", "Divination", "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback exiles Arcane Infusion after it resolves")
    void flashbackExilesAfterResolving() {
        Card spell = new ArcaneInfusion();
        setupTopCards(List.of(new GrizzlyBears(), new Island(), new Swamp(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Arcane Infusion");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void cast() {
        harness.setHand(player1, List.of(new ArcaneInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(cards);
    }
}
