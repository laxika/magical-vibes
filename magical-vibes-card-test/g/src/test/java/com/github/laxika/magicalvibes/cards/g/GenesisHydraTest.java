package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.CarnifexDemon;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GenesisHydra.class, GrizzlyBears.class, Forest.class, Blaze.class, CarnifexDemon.class})
class GenesisHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger reveals X cards; a nonland permanent goes to the battlefield and the rest are shuffled back")
    void castTriggerPutsPermanentOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card blaze = new Blaze();

        harness.setLibrary(player1, List.of(bears, forest, blaze));

        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3); // {X}{G}{G} with X=3

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, blaze);

        harness.passBothPriorities();
        Permanent hydra = findPermanent(player1, "Genesis Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands and cards with mana value greater than X are not offered")
    void ineligibleCardsAreShuffledBackWithoutAPrompt() {
        Card forest = new Forest();
        Card demon = new CarnifexDemon();

        harness.setLibrary(player1, List.of(forest, demon));

        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // X=2, so the demon is too expensive

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, demon);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Carnifex Demon");
    }

    @Test
    @DisplayName("Casting with X=0 reveals nothing and the hydra dies as a 0/0")
    void xZeroRevealsNothingAndHydraDies() {
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Genesis Hydra");
        harness.assertInGraveyard(player1, "Genesis Hydra");
    }

    @Test
    @DisplayName("The optional battlefield choice may be declined")
    void mayDeclineEligiblePermanent() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card unrevealed = new Blaze();
        harness.setLibrary(player1, List.of(bears, forest, unrevealed));
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, forest, unrevealed);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Genesis Hydra").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("A permanent with mana value exactly X is eligible and only one may be selected")
    void manaValueBoundaryAndSingleSelection() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() ->
                harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.assertNotOnBattlefield(player1, "Genesis Hydra");
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Genesis Hydra").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("A revealed Genesis Hydra enters with zero counters and does not trigger its cast ability")
    void revealedHydraUsesZeroForX() {
        Card revealedHydra = new GenesisHydra();
        Card forest = new Forest();
        Card unrevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealedHydra, forest, unrevealed));
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(revealedHydra.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealedHydra);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, unrevealed);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Genesis Hydra");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Genesis Hydra")).isEqualTo(1);
        assertThat(findPermanent(player1, "Genesis Hydra").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @CardUsed(CosisTrickster.class)
    @DisplayName("X equal to zero still shuffles the library and triggers Cosi's Trickster")
    void xZeroStillTriggersShuffleAbilities() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @CardUsed(CosisTrickster.class)
    @DisplayName("An empty library still gets shuffled when the cast trigger resolves")
    void emptyLibraryStillTriggersShuffleAbilities() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({CosisTrickster.class, ElvishMystic.class})
    @DisplayName("Selecting the only revealed card still shuffles the rest of the library")
    void selectingOnlyRevealedCardStillShuffles() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        Card mystic = new ElvishMystic();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(mystic, forest));
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(mystic.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Elvish Mystic");
        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @CardUsed(Dissipate.class)
    @DisplayName("Countering Genesis Hydra does not counter its independent cast trigger")
    void castTriggerResolvesAfterHydraIsCountered() {
        Card hydra = new GenesisHydra();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        harness.setHand(player1, List.of(hydra));
        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Genesis Hydra");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }
}
