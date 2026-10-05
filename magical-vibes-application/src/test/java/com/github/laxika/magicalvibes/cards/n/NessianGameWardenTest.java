package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NessianGameWarden.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class NessianGameWardenTest extends BaseCardTest {

    @Test
    void looksAtAsManyCardsAsForestsControlled() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupLibrary(new Shock(), new GrizzlyBears(), new HillGiant());

        resolveWarden();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    void choosingCreaturePutsItInHandAndOrdersTheRestOnBottom() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        setupLibrary(shock, bears, new HillGiant());

        resolveWarden();
        chooseCard(0);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Hill Giant", "Shock");
    }

    @Test
    void noCreatureAmongLookedAtCardsMovesThemToTheBottomWithoutPrompt() {
        harness.addToBattlefield(player1, new Forest());
        setupLibrary(new Shock(), new HillGiant());

        resolveWarden();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Hill Giant", "Shock");
    }

    @Test
    void canDeclineCreatureAndOrderAllLookedAtCardsOnBottom() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Card creature = new GrizzlyBears();
        Card spell = new Shock();
        Card untouched = new HillGiant();
        setupLibrary(creature, spell, untouched);

        resolveWarden();
        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, spell, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void revealsOnlyChosenCreatureAndAllowsOrderingMultipleRemainingCards() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card spell = new Shock();
        Card untouched = new Forest();
        setupLibrary(bears, giant, spell, untouched);

        resolveWarden();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals"));
        chooseCard(1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(giant);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Hill Giant"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals")
                        && (log.contains("Grizzly Bears") || log.contains("Shock")));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, spell, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noForestsLooksAtNothingEvenWhenOpponentControlsForests() {
        harness.addToBattlefield(player2, new Forest());
        Card creature = new GrizzlyBears();
        Card spell = new Shock();
        setupLibrary(creature, spell);

        resolveWarden();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void libraryShorterThanForestCountStillAllowsChoosingItsOnlyCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Card creature = new GrizzlyBears();
        setupLibrary(creature);

        resolveWarden();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutPrompt() {
        harness.addToBattlefield(player1, new Forest());
        setupLibrary();

        resolveWarden();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countsForestsWhenTriggerResolvesRatherThanWhenCreatureEnters() {
        harness.addToBattlefield(player1, new Forest());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        setupLibrary(first, second, new Shock());
        harness.setHand(player1, List.of(new NessianGameWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
    }

    @Test
    void noMatchingCreaturesStillAllowsOrderingMultipleCardsOnBottom() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Card spell = new Shock();
        Card land = new Forest();
        Card untouched = new GrizzlyBears();
        setupLibrary(spell, land, untouched);

        resolveWarden();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, land, spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveWarden() {
        harness.setHand(player1, List.of(new NessianGameWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.handleCardChosen(player1, index);
    }

    private void setupLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
