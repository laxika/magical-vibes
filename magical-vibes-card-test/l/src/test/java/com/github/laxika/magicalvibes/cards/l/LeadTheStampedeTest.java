package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeadTheStampede.class, LlanowarElves.class, GrizzlyBears.class, Shock.class, Plains.class, Swamp.class})
class LeadTheStampedeTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Lead the Stampede puts it on the stack")
    void castingPutsOnStack() {
        LeadTheStampede spell = new LeadTheStampede();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(spell);
    }

    @Test
    @DisplayName("Resolves by offering multi-select of creature cards among top five")
    void resolvesOfferingMultiSelectOfCreatures() {
        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing multiple creature cards puts them all into hand then reorders rest")
    void choosingMultipleCreaturesThenReorderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(elves, shock, bears, plains, swamp));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose both creature cards
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId(), bears.getId()));

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);

        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        int iShock = indexOf(remaining, "Shock");
        int iPlains = indexOf(remaining, "Plains");
        int iSwamp = indexOf(remaining, "Swamp");
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(iShock, iPlains, iSwamp)));

        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Shock", "Plains", "Swamp");
    }

    @Test
    @DisplayName("Choosing a single creature card puts it into hand then reorders rest")
    void choosingSingleCreatureThenReorderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(elves, shock, bears, plains, swamp));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose only one creature
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .doesNotContain("Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("You may choose no creature cards and still reorder all looked cards to bottom")
    void mayChooseNoCreature() {
        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        // Choose no creatures (empty list)
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no creature cards, directly reorder them to bottom")
    void noCreaturesDirectlyReordersBottom() {
        harness.setLibrary(player1, List.of(
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With empty library, Lead the Stampede does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Lead the Stampede goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(elves, new Shock(), bears, new Plains(), new Swamp()));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId(), bears.getId()));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        harness.assertInGraveyard(player1, "Lead the Stampede");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All five creature cards can be selected")
    void allFiveCreaturesCanBeSelected() {
        LlanowarElves elves1 = new LlanowarElves();
        LlanowarElves elves2 = new LlanowarElves();
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(elves1, elves2, bears1, bears2, bears3));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose all five creature cards
        harness.handleMultipleCardsChosen(player1,
                List.of(elves1.getId(), elves2.getId(), bears1.getId(), bears2.getId(), bears3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        // No remaining cards to reorder - should auto-resolve
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library allows selecting a creature and bottoms the single remaining card")
    void shortLibraryWithOneRemainingCard() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(elves, shock));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Lead the Stampede");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Llanowar Elves"));
    }

    @Test
    @DisplayName("Only the top five cards are examined and the rest go below untouched cards in chosen order")
    void untouchedCardsStayAboveReorderedRemainder() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        GrizzlyBears sixth = new GrizzlyBears();
        Shock seventh = new Shock();
        harness.setLibrary(player1, List.of(elves, shock, bears, plains, swamp, sixth, seventh));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(sixth.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(sixth, seventh, swamp, plains, bears, shock);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Lead the Stampede");
    }

    @Test
    @DisplayName("Noncreature cards cannot be selected and declining preserves all looked cards")
    void noncreatureSelectionIsRejectedAndMayDecline() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(elves, shock, plains));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(shock.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, shock, elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Lead the Stampede");
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
