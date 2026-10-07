package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunbirdsInvocation.class, GrizzlyBears.class, LlanowarElves.class, Memnite.class,
        Shock.class, Forest.class, Mountain.class, Plains.class, Blaze.class, Fling.class})
class SunbirdsInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from hand triggers Sunbird's Invocation")
    void castFromHandTriggers() {
        setupSunbirdsOnBattlefield();

        // Cast a creature from hand (Grizzly Bears — MV 2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Stack should have: creature spell + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getDescription()).contains("Sunbird's Invocation");
    }

    @Test
    @DisplayName("Reveals top X cards where X is the triggering spell's mana value")
    void revealsTopXCards() {
        setupSunbirdsOnBattlefield();

        // Put known cards on top of library (MV 2 spell → reveal 2)
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new GrizzlyBears(), new Mountain(), new Forest()));

        // Cast Grizzly Bears (MV 2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve trigger (top of stack)
        harness.passBothPriorities();

        // Should be awaiting library search with castable cards
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();

        // Only Llanowar Elves (MV 1) should be castable (≤ 2, non-land);
        // Grizzly Bears (MV 2) is also castable
        List<String> castableNames = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream()
                .map(Card::getName).toList();
        assertThat(castableNames).containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");

        // Library should have 2 fewer cards (2 revealed)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a card casts it without paying its mana cost")
    void choosingCardCastsIt() {
        setupSunbirdsOnBattlefield();

        // Put Llanowar Elves (MV 1) and a land on top of library
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new Forest(), new Mountain()));

        // Cast Grizzly Bears (MV 2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve trigger
        harness.passBothPriorities();

        // Choose Llanowar Elves (index 0)
        harness.handleCardChosen(player1, 0);

        // Llanowar Elves should be on the stack as a creature spell (cast without paying)
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getName().equals("Llanowar Elves")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Player may decline to cast and all cards go to bottom")
    void declineToCast() {
        setupSunbirdsOnBattlefield();

        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new GrizzlyBears(), new Mountain()));

        // Cast Grizzly Bears (MV 2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve trigger
        harness.passBothPriorities();

        // Decline (index -1)
        harness.handleCardChosen(player1, -1);

        // All 2 revealed cards should be on the bottom of the library
        // (only Mountain was NOT revealed — it stayed in library)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Lands are not offered as castable choices")
    void landsNotCastable() {
        setupSunbirdsOnBattlefield();

        // Put only lands on top of library
        harness.setLibrary(player1, List.of(
                new Forest(), new Mountain(), new Plains()));

        // Cast Grizzly Bears (MV 2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve trigger
        harness.passBothPriorities();

        // No castable cards (all lands) — should NOT be awaiting library search
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();

        // All cards should be on bottom of library (random order)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cards with mana value greater than X are not offered")
    void manaValueFilter() {
        setupSunbirdsOnBattlefield();

        // Shock is MV 1, Grizzly Bears is MV 2
        // Put Grizzly Bears on top — if we cast a spell with MV 1, Bears (MV 2) should not be offered
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest()));

        // Cast Llanowar Elves (MV 1) — reveal 1 card
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        // Resolve trigger
        harness.passBothPriorities();

        // Grizzly Bears MV 2 > MV 1 → no castable cards
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when casting a spell not from hand (e.g., from Sunbird's itself)")
    void doesNotTriggerForNonHandCasts() {
        setupSunbirdsOnBattlefield();

        // Put known cards on top
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new Shock(), new Mountain()));

        // Cast Shock (MV 1) from hand — triggers Sunbird's
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve Sunbird's trigger (it's on top of stack above Shock)
        harness.passBothPriorities();

        // Should be awaiting library search (1 card revealed — Llanowar Elves MV 1 ≤ 1)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Choose Llanowar Elves (index 0) — cast without paying
        harness.handleCardChosen(player1, 0);

        // Llanowar Elves should be on the stack
        // The Sunbird's trigger should NOT fire again for this free cast (not from hand)
        long sunbirdTriggerCount = gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getDescription().contains("Sunbird's Invocation"))
                .count();
        assertThat(sunbirdTriggerCount).isZero();
    }

    @Test
    @DisplayName("Remaining revealed cards go to bottom of library (not awaiting reorder)")
    void remainingCardsToBottomNotReorder() {
        setupSunbirdsOnBattlefield();

        // 3 cards on top: LlanowarElves, GrizzlyBears, Mountain
        LlanowarElves elf = new LlanowarElves();
        GrizzlyBears bears = new GrizzlyBears();
        Mountain mountain = new Mountain();
        Forest extraForest = new Forest();
        harness.setLibrary(player1, List.of(elf, bears, mountain, extraForest));

        // Cast a 3-MV spell from hand to reveal 3 cards
        // Use Shock (MV 1) + add extra mana to cast something bigger...
        // Actually let's just use Grizzly Bears (MV 2) to reveal 2
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve trigger
        harness.passBothPriorities();

        // Choose LlanowarElves (index 0) from revealed [LlanowarElves, GrizzlyBears]
        harness.handleCardChosen(player1, 0);

        // Should NOT be awaiting LIBRARY_REORDER (random order, no player interaction)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNull();

        // Remaining revealed card (GrizzlyBears) + extraForest + Mountain should be in library
        // GrizzlyBears was remaining after choosing LlanowarElves, placed on bottom
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("MV 0 spell reveals no cards and library is unchanged")
    void manaValueZeroRevealsNothing() {
        setupSunbirdsOnBattlefield();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Cast Memnite (MV 0) — triggers Sunbird's but reveals 0 cards
        harness.setHand(player1, List.of(new Memnite()));
        harness.castCreature(player1, 0);

        // Stack: creature spell + triggered ability
        assertThat(gd.stack).hasSize(2);

        // Resolve Sunbird's trigger — should reveal 0 cards, nothing happens
        harness.passBothPriorities();

        // Library size unchanged (no cards revealed)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        // Should not be awaiting library search
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Opponent casting a spell does not trigger Sunbird's Invocation")
    void opponentCastDoesNotTrigger() {
        setupSunbirdsOnBattlefield();

        // Give opponent a spell to cast
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        // Stack should only have the creature spell, no Sunbird's trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The chosen X contributes to the mana value of the triggering spell")
    void triggeringSpellIncludesChosenX() {
        setupSunbirdsOnBattlefield();
        Forest first = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Mountain third = new Mountain();
        Plains fourth = new Plains();
        harness.setLibrary(player1, List.of(first, bears, third, fourth));
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(bears);
    }

    @Test
    @DisplayName("A short library reveals all available cards without drawing or losing")
    void shortLibraryRevealsAvailableCards() {
        setupSunbirdsOnBattlefield();
        LlanowarElves elf = new LlanowarElves();
        harness.setLibrary(player1, List.of(elf));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(elf);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
    }

    @Test
    @DisplayName("An empty library makes the trigger do nothing")
    void emptyLibraryDoesNothing() {
        setupSunbirdsOnBattlefield();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining keeps unrevealed cards on top and bottoms only the revealed cards")
    void declinePreservesUnrevealedOrder() {
        setupSunbirdsOnBattlefield();
        LlanowarElves elf = new LlanowarElves();
        GrizzlyBears bears = new GrizzlyBears();
        Mountain mountain = new Mountain();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(elf, bears, mountain, forest));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(mountain, forest);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(elf, bears);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A revealed instant chooses a target and resolves before the original spell")
    void revealedInstantResolvesBeforeOriginalSpell() {
        setupSunbirdsOnBattlefield();
        Shock shock = new Shock();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(shock, forest, mountain));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isSameAs(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain, forest);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting Fling for free still pays its sacrifice cost and uses the creature's power")
    void freeCastPaysMandatorySacrificeCost() {
        setupSunbirdsOnBattlefield();
        harness.addToBattlefield(player1, new GrizzlyBears());
        var sacrificeId = harness.getPermanentId(player1, "Grizzly Bears");
        Fling fling = new Fling();
        harness.setLibrary(player1, List.of(fling, new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        for (int choice = 0; choice < 2 && gd.interaction.isAwaitingInput(); choice++) {
            var pending = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(pending).isNotNull();
            harness.handlePermanentChosen(player1,
                    pending.validIds().contains(player2.getId()) ? player2.getId() : sacrificeId);
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrificeId));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack.getLast().getCard()).isSameAs(fling);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    private void setupSunbirdsOnBattlefield() {
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SunbirdsInvocation());
    }
}
