package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebukingCeremony.class, DarksteelBrute.class, DarksteelIngot.class, CrazedGoblin.class})
class RebukingCeremonyTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Puts both target artifacts on top of their owners' libraries")
    void putsBothTargetArtifactsOnTopOfLibraries() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();
        UUID darksteelIngotId = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot()).getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(darksteelBruteId, darksteelIngotId));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).isEmpty();
        List<Card> deck = gameData.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 2);
        assertThat(deck.stream().limit(2).map(Card::getName))
                .containsExactlyInAnyOrder("Darksteel Brute", "Darksteel Ingot");
    }

    @Test
    @DisplayName("Puts each artifact on top of its own owner's library")
    void putsArtifactsOnTheirOwnersLibraries() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute()).getId();
        UUID darksteelIngotId = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot()).getId();
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(darksteelBruteId, darksteelIngotId));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Darksteel Brute");
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore + 1);
        assertThat(gameData.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Darksteel Ingot");
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(player2DeckSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();
        UUID crazedGoblinId = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin()).getId();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(darksteelBruteId, crazedGoblinId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same artifact twice")
    void cannotTargetSameArtifactTwice() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(darksteelBruteId, darksteelBruteId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The artifacts' owner chooses their order on top of the library")
    void ownerChoosesLibraryOrder() {
        DarksteelBrute brute = new DarksteelBrute();
        DarksteelIngot ingot = new DarksteelIngot();
        UUID bruteId = harness.addToBattlefieldAndReturn(player2, brute).getId();
        UUID ingotId = harness.addToBattlefieldAndReturn(player2, ingot).getId();
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(bruteId, ingotId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.playerId()).isEqualTo(player2.getId());
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.toBottom()).isFalse();
        assertThat(reorder.cards()).containsExactlyInAnyOrder(brute, ingot);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(brute), reorder.cards().indexOf(ingot))));

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2)).containsExactly(brute, ingot);
        assertThat(gd.playerDecks.get(player2.getId()).subList(2, originalDeck.size() + 2))
                .containsExactlyElementsOf(originalDeck);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rebuking Ceremony");
    }

    @Test
    @DisplayName("Requires exactly two target artifacts")
    void cannotCastWithOnlyOneTarget() {
        UUID bruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();
        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bruteId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still puts the remaining legal target on top when the first target leaves")
    void resolvesWithOneLegalTarget() {
        UUID bruteId = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute()).getId();
        DarksteelIngot ingot = new DarksteelIngot();
        UUID ingotId = harness.addToBattlefieldAndReturn(player2, ingot).getId();
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player2.getId()));
        List<Card> originalOwnDeck = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(bruteId, ingotId));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(ingot);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, originalDeck.size() + 1))
                .containsExactlyElementsOf(originalDeck);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalOwnDeck);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rebuking Ceremony");
    }

    @Test
    @DisplayName("Does not resolve when both targets have left the battlefield")
    void doesNotResolveWithoutLegalTargets() {
        UUID bruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();
        UUID ingotId = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot()).getId();
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(bruteId, ingotId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(originalDeck);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rebuking Ceremony");
    }
}
