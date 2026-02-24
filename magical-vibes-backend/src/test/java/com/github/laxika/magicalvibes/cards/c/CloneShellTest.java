package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CloneShellTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has imprint ETB effect and dies trigger effect")
    void hasCorrectEffects() {
        CloneShell card = new CloneShell();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ImprintFromTopCardsEffect.class);
        ImprintFromTopCardsEffect etb = (ImprintFromTopCardsEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etb.count()).isEqualTo(4);

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(PutImprintedCreatureOntoBattlefieldEffect.class);
    }

    // ===== ETB imprint =====

    @Test
    @DisplayName("ETB presents top 4 cards for imprint choice")
    void etbPresentsTopFourCards() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new Plains(), new LlanowarElves()));
        harness.setHand(player1, List.of(new CloneShell()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(4);
    }

    @Test
    @DisplayName("Choosing a card exiles it and imprints on Clone Shell")
    void choosingCardExilesAndImprints() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Plains plains = new Plains();
        LlanowarElves elves = new LlanowarElves();
        setupTopCards(List.of(bears, shock, plains, elves));
        harness.setHand(player1, List.of(new CloneShell()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        // Choose the first card (Grizzly Bears)
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Clone Shell should have it imprinted
        Permanent cloneShell = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Clone Shell"))
                .findFirst().orElseThrow();
        assertThat(cloneShell.getCard().getImprintedCard()).isNotNull();
        assertThat(cloneShell.getCard().getImprintedCard().getName()).isEqualTo("Grizzly Bears");

        // Remaining 3 cards should be awaiting reorder
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(3);
    }

    @Test
    @DisplayName("After reorder, remaining cards go to bottom of library")
    void remainingCardsGoToBottom() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Plains plains = new Plains();
        LlanowarElves elves = new LlanowarElves();
        setupTopCards(List.of(bears, shock, plains, elves));
        harness.setHand(player1, List.of(new CloneShell()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0); // exile Grizzly Bears

        // Reorder remaining: Shock(0), Plains(1), Llanowar Elves(2)
        List<Card> remaining = gd.interaction.awaitingLibraryReorderCards();
        int iShock = indexOf(remaining, "Shock");
        int iPlains = indexOf(remaining, "Plains");
        int iElves = indexOf(remaining, "Llanowar Elves");
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(iPlains, iElves, iShock));

        // Cards should be on the bottom of the library in the chosen order
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(3);
        assertThat(deck.get(0).getName()).isEqualTo("Plains");
        assertThat(deck.get(1).getName()).isEqualTo("Llanowar Elves");
        assertThat(deck.get(2).getName()).isEqualTo("Shock");
    }

    // ===== Dies trigger — creature imprinted =====

    @Test
    @DisplayName("Dies trigger puts imprinted creature onto the battlefield")
    void diesTriggerPutsCreatureOntoBattlefield() {
        CloneShell shellCard = new CloneShell();
        harness.addToBattlefield(player1, shellCard);

        GameData gd = harness.getGameData();
        Permanent cloneShell = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Clone Shell"))
                .findFirst().orElseThrow();

        // Manually imprint a creature card
        GrizzlyBears bears = new GrizzlyBears();
        cloneShell.getCard().setImprintedCard(bears);
        gd.playerExiledCards.get(player1.getId()).add(bears);

        // Kill Clone Shell with Shock (2 damage to a 2/2)
        UUID cloneShellId = harness.getPermanentId(player1, "Clone Shell");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, cloneShellId);
        harness.passBothPriorities(); // resolve Shock — Clone Shell dies
        harness.passBothPriorities(); // resolve death trigger

        // Grizzly Bears should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Grizzly Bears should be removed from exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Clone Shell should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Clone Shell"));
    }

    // ===== Dies trigger — non-creature imprinted =====

    @Test
    @DisplayName("Dies trigger does nothing if imprinted card is not a creature")
    void diesTriggerDoesNothingForNonCreature() {
        CloneShell shellCard = new CloneShell();
        harness.addToBattlefield(player1, shellCard);

        GameData gd = harness.getGameData();
        Permanent cloneShell = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Clone Shell"))
                .findFirst().orElseThrow();

        // Manually imprint a non-creature card
        Spellbook spellbook = new Spellbook();
        cloneShell.getCard().setImprintedCard(spellbook);
        gd.playerExiledCards.get(player1.getId()).add(spellbook);

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Kill Clone Shell with Shock (2 damage to a 2/2)
        UUID cloneShellId = harness.getPermanentId(player1, "Clone Shell");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, cloneShellId);
        harness.passBothPriorities(); // resolve Shock — Clone Shell dies
        harness.passBothPriorities(); // resolve death trigger

        // No new permanent on battlefield (Clone Shell removed, nothing added)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore - 1);

        // Spellbook should remain in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    // ===== Dies trigger — no imprint =====

    @Test
    @DisplayName("Dies trigger does nothing if no card was imprinted")
    void diesTriggerDoesNothingWithNoImprint() {
        CloneShell shellCard = new CloneShell();
        harness.addToBattlefield(player1, shellCard);

        GameData gd = harness.getGameData();

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Kill Clone Shell without imprinting anything
        UUID cloneShellId = harness.getPermanentId(player1, "Clone Shell");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, cloneShellId);
        harness.passBothPriorities(); // resolve Shock — Clone Shell dies
        harness.passBothPriorities(); // resolve death trigger

        // No new permanent on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore - 1);
    }

    // ===== ETB with empty library =====

    @Test
    @DisplayName("ETB does nothing with empty library")
    void etbDoesNothingWithEmptyLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new CloneShell()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Helpers =====

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private int indexOf(List<Card> cards, String name) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Card not found in list: " + name);
    }
}
