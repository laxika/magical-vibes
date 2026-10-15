package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.n.NihilisticGlee;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HideSeek.class, AzoriusSignet.class, AssaultZeppelid.class, NihilisticGlee.class})
class HideSeekTest extends BaseCardTest {

    private static final int HIDE = 0;
    private static final int SEEK = 1;

    @Test
    @DisplayName("Hide puts a target artifact on the bottom of its owner's library")
    void hidePutsArtifactOnBottomOfLibrary() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        AssaultZeppelid libraryCard = new AssaultZeppelid();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, HIDE, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Azorius Signet");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, artifact.getCard());
    }

    @Test
    @DisplayName("Hide puts a target enchantment on the bottom of its owner's library")
    void hidePutsEnchantmentOnBottomOfLibrary() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new NihilisticGlee());
        AssaultZeppelid libraryCard = new AssaultZeppelid();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, HIDE, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nihilistic Glee");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, enchantment.getCard());
    }

    @Test
    @DisplayName("Hide cannot target a creature")
    void hideCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, HIDE, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Seek cannot target its controller's library")
    void seekCannotTargetOwnLibrary() {
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, SEEK, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Seek gains no life and creates no search when the opponent's library is empty")
    void seekWithEmptyLibraryDoesNothingBeyondShuffling() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, SEEK, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Seek exiles a card from an opponent's library and gains its mana value")
    void seekExilesCardAndGainsManaValueAsLife() {
        AzoriusSignet chosen = new AzoriusSignet();
        AssaultZeppelid remaining = new AssaultZeppelid();
        harness.setLibrary(player2, List.of(chosen, remaining));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, SEEK, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chosen);
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    void hideReturnsOpponentOwnedArtifactToItsOwnersLibrary() {
        AzoriusSignet card = new AzoriusSignet();
        card.setOwnerId(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, card);
        AssaultZeppelid remaining = new AssaultZeppelid();
        harness.setLibrary(player2, List.of(remaining));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, HIDE, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Azorius Signet");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining, card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void seekGainsCombinedManaValueOfExiledSplitCard() {
        HideSeek chosen = new HideSeek();
        harness.setLibrary(player2, List.of(chosen));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, SEEK, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void seekMustSelectACardFromNonemptyLibrary() {
        AzoriusSignet chosen = new AzoriusSignet();
        harness.setLibrary(player2, List.of(chosen));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, SEEK, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        harness.assertLife(player1, 22);
    }
}
