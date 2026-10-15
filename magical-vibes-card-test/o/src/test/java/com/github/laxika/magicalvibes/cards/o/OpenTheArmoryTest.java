package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.m.MurderersAxe;
import com.github.laxika.magicalvibes.cards.g.GryffsBoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpenTheArmory.class, MurderersAxe.class, GryffsBoon.class, DevilthornFox.class})
class OpenTheArmoryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only Aura and Equipment cards from the library")
    void offersOnlyAurasOrEquipment() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getSubtypes().contains(CardSubtype.AURA)
                        || card.getSubtypes().contains(CardSubtype.EQUIPMENT));
    }

    @Test
    @DisplayName("Choosing a card puts it into hand")
    void choosingPutsCardIntoHand() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Card chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No interaction occurs when the library has no Aura or Equipment")
    void noMatchNoInteraction() {
        harness.setLibrary(player1, List.of(new DevilthornFox()));

        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An Aura is revealed and moved from the library into hand")
    void findsAura() {
        GryffsBoon aura = new GryffsBoon();
        DevilthornFox creature = new DevilthornFox();
        harness.setLibrary(player1, List.of(aura, creature));
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(message -> message.contains("reveals Gryff's Boon"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find even when eligible cards exist")
    void canFailToFind() {
        MurderersAxe equipment = new MurderersAxe();
        GryffsBoon aura = new GryffsBoon();
        harness.setLibrary(player1, List.of(equipment, aura));
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equipment, aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(message -> message.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("An empty library completes the search without a choice")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        cast();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void cast() {
        harness.castFromHand(player1, new OpenTheArmory(), "{1}{W}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new MurderersAxe(),
                new GryffsBoon(),
                new DevilthornFox()));
    }
}
