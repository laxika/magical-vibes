package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EarthshakerDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuriedTreasure.class, Forest.class, GrizzlyBears.class, EarthshakerDreadmaw.class, PanickedAltisaur.class})
class BuriedTreasureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Buried Treasure adds one mana of the chosen color")
    void sacrificesForAnyColorMana() {
        harness.addToBattlefield(player1, new BuriedTreasure());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Buried Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graveyard ability exiles Buried Treasure and discovers a qualifying card")
    void discoversFromGraveyard() {
        prepareGraveyardAbility(new Forest(), new GrizzlyBears());

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotInGraveyard(player1, "Buried Treasure");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Buried Treasure"));
    }

    @Test
    @DisplayName("Declining a discovered card puts it into hand")
    void declinesDiscoveredCardToHand() {
        prepareGraveyardAbility(new GrizzlyBears());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Graveyard ability can only be activated at sorcery speed")
    void graveyardAbilityRequiresSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new BuriedTreasure()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Buried Treasure");
    }

    @Test
    void manaAbilityResolvesWithoutUsingTheStackForEveryColor() {
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addToBattlefield(player1, new BuriedTreasure());
            harness.activateAbility(player1, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            harness.assertNotOnBattlefield(player1, "Buried Treasure");
        }
    }

    @Test
    void tappedTreasureCannotPayTheTapCost() {
        harness.addToBattlefieldAndReturn(player1, new BuriedTreasure()).setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Buried Treasure");
        harness.assertNotInGraveyard(player1, "Buried Treasure");
    }

    @Test
    void discoverSkipsLandsAndCardsAboveFiveAndReturnsThemBelowTheUntouchedLibrary() {
        Forest land = new Forest();
        EarthshakerDreadmaw expensive = new EarthshakerDreadmaw();
        BuriedTreasure hit = new BuriedTreasure();
        Forest untouched = new Forest();
        prepareGraveyardAbility(land, expensive, hit, untouched);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Buried Treasure");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, expensive);
    }

    @Test
    void discoveredCardsAreInExileWhileChoosingWhetherToCast() {
        Forest land = new Forest();
        BuriedTreasure hit = new BuriedTreasure();
        prepareGraveyardAbility(land, hit);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land, hit);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land, hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInHand(player1, "Buried Treasure");
    }

    @Test
    void discoverWithNoQualifyingCardReturnsAllCardsToTheLibrary() {
        Forest land = new Forest();
        EarthshakerDreadmaw expensive = new EarthshakerDreadmaw();
        prepareGraveyardAbility(land, expensive);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Earthshaker Dreadmaw");
    }

    @Test
    void insufficientManaDoesNotExileTheSource() {
        harness.setGraveyard(player1, List.of(new BuriedTreasure()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Buried Treasure");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void graveyardAbilityCannotBeActivatedOutsideAMainPhase() {
        prepareGraveyardAbility(new BuriedTreasure());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Buried Treasure");
    }

    @Test
    void graveyardAbilityCannotBeActivatedWithANonemptyStack() {
        prepareGraveyardAbility(new BuriedTreasure());
        harness.setGraveyard(player1, List.of(new BuriedTreasure(), new BuriedTreasure()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Buried Treasure");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void discoverCastsACardWithManaValueExactlyFiveWithoutPayingItsManaCost() {
        prepareGraveyardAbility(new PanickedAltisaur());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Panicked Altisaur");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void discoverWithAnEmptyLibraryCompletesWithoutAChoice() {
        prepareGraveyardAbility();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInGraveyard(player1, "Buried Treasure");
    }

    private void prepareGraveyardAbility(Card... library) {
        harness.setGraveyard(player1, List.of(new BuriedTreasure()));
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
