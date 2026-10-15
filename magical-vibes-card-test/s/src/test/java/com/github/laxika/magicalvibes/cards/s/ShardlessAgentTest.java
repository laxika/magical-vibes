package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShardlessAgent.class, GrizzlyBears.class, HillGiant.class, Island.class, Mountain.class, Fling.class})
class ShardlessAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade stops at the first lesser-cost nonland card")
    void cascadeOffersFirstLesserNonlandCard() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(new Mountain(), new HillGiant(), new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears")
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Declining cascade puts the exiled cards on the bottom of the library")
    void decliningCascadeBottomsExiledCards() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Grizzly Bears")
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mountain", "Grizzly Bears");
    }

    @Test
    @DisplayName("Cascade skips equal mana values and the free spell resolves before the Agent")
    void equalManaValueIsSkippedAndFreeSpellResolvesFirst() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        ShardlessAgent skipped = new ShardlessAgent();
        Island untouched = new Island();
        harness.setLibrary(player1, List.of(skipped, new GrizzlyBears(), untouched));
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, skipped);
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Shardless Agent");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shardless Agent");
    }

    @Test
    @DisplayName("Cascade returns the entire library when no card qualifies")
    void noQualifyingCardReturnsEntireLibrary() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        List<Card> library = List.of(new Mountain(), new HillGiant(), new ShardlessAgent());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shardless Agent");
    }

    @Test
    @DisplayName("An empty library does not prevent Shardless Agent from resolving")
    void emptyLibraryDoesNotPreventResolution() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shardless Agent");
    }

    @Test
    @DisplayName("Declining cascade preserves the untouched library above the returned cards")
    void decliningCascadePreservesUntouchedTopCard() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        Mountain skipped = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        Island untouched = new Island();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.findExiledCard(skipped.getId())).isNotNull();
        assertThat(gd.findExiledCard(hit.getId())).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, hit);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cascade still requires Fling's sacrifice cost and uses the sacrificed power")
    void cascadeRequiresAdditionalSacrificeCost() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Fling(), new Island()));
        harness.setHand(player1, List.of(new ShardlessAgent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, sacrifice.getId());
        }

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }
}
