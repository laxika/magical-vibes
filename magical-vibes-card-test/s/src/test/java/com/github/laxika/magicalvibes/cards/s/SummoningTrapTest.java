package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.r.ResilientKhenra;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummoningTrap.class, Cancel.class, GrizzlyBears.class, LlanowarElves.class,
        MightOfOaks.class, ResilientKhenra.class, StoneworkPuma.class})
class SummoningTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Can pay the normal mana cost and select an artifact creature from a short library")
    void normalCostWithShortLibrary() {
        StoneworkPuma puma = new StoneworkPuma();
        Cancel first = new Cancel();
        Cancel second = new Cancel();
        harness.setHand(player1, List.of(new SummoningTrap()));
        harness.setLibrary(player1, List.of(first, puma, second));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        harness.assertOnBattlefield(player1, "Stonework Puma");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        harness.assertInGraveyard(player1, "Summoning Trap");
    }

    @Test
    @DisplayName("May decline a creature and put all looked-at cards below the untouched eighth card")
    void mayDeclineCreature() {
        List<Card> top = List.of(new Cancel(), new StoneworkPuma(), new Cancel(),
                new Cancel(), new Cancel(), new Cancel(), new Cancel());
        StoneworkPuma eighth = new StoneworkPuma();
        List<Card> library = new java.util.ArrayList<>(top);
        library.add(eighth);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SummoningTrap()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(6, 5, 4, 3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                eighth, top.get(6), top.get(5), top.get(4), top.get(3),
                top.get(2), top.get(1), top.get(0));
        harness.assertNotOnBattlefield(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("Cannot select a creature below the top seven")
    void noCreatureAmongTopSeven() {
        List<Card> top = List.of(new Cancel(), new Cancel(), new Cancel(),
                new Cancel(), new Cancel(), new Cancel(), new Cancel());
        StoneworkPuma eighth = new StoneworkPuma();
        List<Card> library = new java.util.ArrayList<>(top);
        library.add(eighth);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SummoningTrap()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5, 6)));

        library.clear();
        library.add(eighth);
        library.addAll(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertNotOnBattlefield(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("An empty library resolves without requiring a selection")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SummoningTrap()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Summoning Trap");
    }

    @Test
    @DisplayName("Can be cast for no mana after an opponent counters your creature spell")
    void castsForFreeAfterOpponentCountersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        SummoningTrap trap = new SummoningTrap();
        harness.setHand(player1, List.of(bears, trap));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new MightOfOaks(), new LlanowarElves(), new MightOfOaks(),
                new LlanowarElves(), new MightOfOaks(), new LlanowarElves()));

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5)));
    }

    @Test
    @DisplayName("Returns the remaining cards before the found creature's triggered ability")
    void returnsRemainingCardsBeforeFoundCreatureTriggeredAbility() {
        GrizzlyBears counteredCreature = new GrizzlyBears();
        SummoningTrap trap = new SummoningTrap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(counteredCreature, trap));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counteredCreature.getId());
        harness.passBothPriorities();

        ResilientKhenra khenra = new ResilientKhenra();
        List<Card> remaining = List.of(
                new LlanowarElves(), new MightOfOaks(), new LlanowarElves(),
                new MightOfOaks(), new LlanowarElves(), new MightOfOaks());
        List<Card> library = new java.util.ArrayList<>();
        library.add(khenra);
        library.addAll(remaining);
        harness.setLibrary(player1, library);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(remaining);
        assertThat(gd.pendingLibraryBottomReorders).isEmpty();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The alternate cost requires an opponent to have countered a creature spell")
    void alternateCostRequiresOpponentCounteredCreatureSpell() {
        SummoningTrap trap = new SummoningTrap();
        harness.setHand(player1, List.of(trap));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A countered noncreature spell does not enable the alternate cost")
    void noncreatureSpellDoesNotEnableAlternateCost() {
        GrizzlyBears bears = new GrizzlyBears();
        MightOfOaks might = new MightOfOaks();
        SummoningTrap trap = new SummoningTrap();
        harness.addToBattlefield(player1, bears);
        harness.setHand(player1, List.of(might, trap));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
