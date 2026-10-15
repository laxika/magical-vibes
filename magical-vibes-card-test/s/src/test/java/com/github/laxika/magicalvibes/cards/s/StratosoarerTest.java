package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronShieldElf;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stratosoarer.class, IronShieldElf.class, Plains.class, Forest.class, Island.class, EvolvingWilds.class})
class StratosoarerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants flying to target creature until end of turn")
    void etbGrantsFlyingUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new IronShieldElf());
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearches() {
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stratosoarer");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    @Test
    @DisplayName("Choosing a basic land from basic landcycling puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals(chosenName));
    }

    @Test
    @DisplayName("ETB can grant flying to a creature controlled by its controller")
    void etbCanTargetOwnCreature() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new IronShieldElf());
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, elf.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Basic landcycling discards as a cost before the search resolves")
    void basicLandcyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Stratosoarer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Basic landcycling requires blue mana and does not discard on failed payment")
    void basicLandcyclingRequiresBlueMana() {
        Stratosoarer card = new Stratosoarer();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Stratosoarer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling can fail to find even with a basic land in the library")
    void basicLandcyclingCanFailToFind() {
        Island island = new Island();
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.setLibrary(player1, List.of(island));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        harness.assertInGraveyard(player1, "Stratosoarer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling resolves without drawing when no basic lands exist")
    void basicLandcyclingWithNoBasicLands() {
        IronShieldElf elf = new IronShieldElf();
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.setLibrary(player1, List.of(elf));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
        harness.assertInGraveyard(player1, "Stratosoarer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new IronShieldElf()));
    }

    @Test
    @DisplayName("Basic landcycling excludes nonbasic lands")
    void basicLandcyclingExcludesNonbasicLands() {
        Island island = new Island();
        EvolvingWilds wilds = new EvolvingWilds();
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.setLibrary(player1, List.of(wilds, island));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(island);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wilds);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Basic landcycling resolves with an empty library")
    void basicLandcyclingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new Stratosoarer()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Stratosoarer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
