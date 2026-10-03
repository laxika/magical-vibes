package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloneShell.class, GrizzlyBears.class, LlanowarElves.class, Plains.class,
        Shock.class, Spellbook.class, Panharmonicon.class})
class CloneShellTest extends BaseCardTest {

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(4);
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
        harness.handleCardChosen(player1, 0);

        // Card should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Clone Shell should have it imprinted
        Permanent cloneShell = findPermanent(player1, "Clone Shell");
        assertThat(gd.getImprintedCard(cloneShell.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(cloneShell.getCard()).getName()).isEqualTo("Grizzly Bears");

        // Remaining 3 cards should be awaiting reorder
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
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
        harness.handleCardChosen(player1, 0); // exile Grizzly Bears

        // Reorder remaining: Shock(0), Plains(1), Llanowar Elves(2)
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        int iShock = indexOf(remaining, "Shock");
        int iPlains = indexOf(remaining, "Plains");
        int iElves = indexOf(remaining, "Llanowar Elves");
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(iPlains, iElves, iShock)));

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
        Permanent cloneShell = findPermanent(player1, "Clone Shell");

        // Manually imprint a creature card
        GrizzlyBears bears = new GrizzlyBears();
        gd.setImprintedCard(cloneShell.getCard(), bears);
        gd.addToExile(player1.getId(), bears);

        // Kill Clone Shell with Shock (2 damage to a 2/2)
        UUID cloneShellId = harness.getPermanentId(player1, "Clone Shell");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, cloneShellId); // resolve Shock — Clone Shell dies
        harness.passBothPriorities(); // resolve death trigger

        // Grizzly Bears should be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Grizzly Bears should be removed from exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Clone Shell should be in graveyard
        harness.assertInGraveyard(player1, "Clone Shell");
    }

    // ===== Dies trigger — non-creature imprinted =====

    @Test
    @DisplayName("Dies trigger does nothing if imprinted card is not a creature")
    void diesTriggerDoesNothingForNonCreature() {
        CloneShell shellCard = new CloneShell();
        harness.addToBattlefield(player1, shellCard);

        GameData gd = harness.getGameData();
        Permanent cloneShell = findPermanent(player1, "Clone Shell");

        // Manually imprint a non-creature card
        Spellbook spellbook = new Spellbook();
        gd.setImprintedCard(cloneShell.getCard(), spellbook);
        gd.addToExile(player1.getId(), spellbook);

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Kill Clone Shell with Shock (2 damage to a 2/2)
        UUID cloneShellId = harness.getPermanentId(player1, "Clone Shell");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, cloneShellId); // resolve Shock — Clone Shell dies
        harness.passBothPriorities(); // resolve death trigger

        // No new permanent on battlefield (Clone Shell removed, nothing added)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore - 1);

        // Spellbook should remain in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
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
        harness.castAndResolveInstant(player2, 0, cloneShellId); // resolve Shock — Clone Shell dies
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

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Helpers =====

    @Test
    @DisplayName("A one-card library is imprinted face down and its creature enters on death")
    void oneCardLibraryImprintsAndReturnsCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        castShellWithLibrary(List.of(bears));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();

        killShell();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("A noncreature imprinted face down is revealed and remains in exile on death")
    void deathRevealsNonCreature() {
        Plains plains = new Plains();
        castShellWithLibrary(List.of(plains));
        assertThat(gd.findExiledCard(plains.getId()).faceDown()).isTrue();

        killShell();

        assertThat(gd.findExiledCard(plains.getId())).isNotNull();
        assertThat(gd.findExiledCard(plains.getId()).faceDown()).isFalse();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("A two-card library exiles the selected card and bottoms the other without reordering")
    void twoCardLibraryExilesOneAndBottomsTheOther() {
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        castShellWithLibrary(List.of(bears, plains));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The death trigger cannot return an imprinted card that has left exile")
    void deathDoesNotReturnCardNoLongerInExile() {
        GrizzlyBears bears = new GrizzlyBears();
        castShellWithLibrary(List.of(bears));
        gd.removeFromExile(bears.getId());
        gd.playerHands.get(player1.getId()).add(bears);

        killShell();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("A doubled imprint trigger returns both exiled creatures when Clone Shell dies")
    void doubledImprintReturnsBothCreatures() {
        harness.addToBattlefield(player1, new Panharmonicon());
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        castShellWithLibrary(List.of(bears, new Plains(), new Plains(), new Plains(), elves));
        harness.handleCardChosen(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(elves.getId()).faceDown()).isTrue();

        killShell();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.findExiledCard(elves.getId())).isNull();
    }

    private void castShellWithLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new CloneShell()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killShell() {
        UUID shellId = harness.getPermanentId(player1, "Clone Shell");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, shellId);
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
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
