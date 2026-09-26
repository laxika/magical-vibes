package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommuneWithNature.class, GrizzlyBears.class, LlanowarElves.class, Plains.class, Shock.class, Swamp.class})
class CommuneWithNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Commune with Nature puts it on the stack")
    void castingPutsOnStack() {
        CommuneWithNature commune = new CommuneWithNature();
        harness.setHand(player1, List.of(commune));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(commune);
    }

    @Test
    @DisplayName("Resolves by offering only creature cards among top five")
    void resolvesOfferingOnlyCreatures() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        setupTopFive(List.of(
                elves, shock, bears, plains, swamp
        ));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactlyInAnyOrder(elves, bears);
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand then orders rest on bottom")
    void choosingCreatureThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        setupTopFive(List.of(elves, shock, bears, plains, swamp));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Llanowar Elves
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(elves);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);

        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        // Put Plains first (closest to top), then Shock, then Swamp, then Grizzly Bears
        int iPlains = remaining.indexOf(plains);
        int iShock = remaining.indexOf(shock);
        int iSwamp = remaining.indexOf(swamp);
        int iBears = remaining.indexOf(bears);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(iPlains, iShock, iSwamp, iBears)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, shock, swamp, bears);
    }

    @Test
    @DisplayName("With fewer than five cards, Commune with Nature considers the whole library")
    void considersShortLibrary() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        setupTopFive(List.of(elves, shock));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(elves);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(elves);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("You may choose no creature card and still reorder all looked cards to bottom")
    void mayChooseNoCreature() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no creature cards, directly reorder them to bottom")
    void noCreaturesDirectlyReordersBottom() {
        setupTopFive(List.of(
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With empty library, Commune with Nature does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Commune with Nature goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        CommuneWithNature commune = new CommuneWithNature();
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(commune));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(commune);
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
