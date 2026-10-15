package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({StainTheMind.class, RuneclawBear.class, Divination.class, BlackCat.class, Swamp.class})
class StainTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles every chosen copy from target player's hand, graveyard and library")
    void exilesChosenCopiesFromAllZones() {
        Card bears1 = new RuneclawBear();
        Card bears2 = new RuneclawBear();
        Card bears3 = new RuneclawBear();
        Card divination = new Divination();

        harness.setHand(player2, List.of(bears1, divination));
        harness.setGraveyard(player2, List.of(bears2));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Runeclaw Bear");
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Runeclaw Bear"))
                .count()).isEqualTo(3);
        harness.assertNotInHand(player2, "Runeclaw Bear");
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Runeclaw Bear"));
        harness.assertInHand(player2, "Divination");
        harness.assertInGraveyard(player1, "Stain the Mind");
    }

    @Test
    @DisplayName("Choosing a name with no matches exiles nothing")
    void noMatchesExilesNothing() {
        harness.setHand(player2, List.of(new Divination()));
        harness.setGraveyard(player2, List.of());

        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Runeclaw Bear");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Convoke taps untapped creatures to pay the generic cost")
    void convokeTapsCreaturesToPayGenericCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new RuneclawBear());
        }
        List<Permanent> helpers = List.copyOf(gd.playerBattlefields.get(player1.getId()));

        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                helpers.stream().map(Permanent::getId).toList());

        assertThat(gd.stack).hasSize(1);
        assertThat(helpers).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("May leave matching cards in all searched zones")
    void mayChooseZeroMatchingCards() {
        Card handBear = new RuneclawBear();
        Card graveyardBear = new RuneclawBear();
        Card libraryBear = new RuneclawBear();
        harness.setHand(player2, List.of(handBear));
        harness.setGraveyard(player2, List.of(graveyardBear));
        harness.setLibrary(player2, List.of(libraryBear));
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Runeclaw Bear");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handBear);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardBear);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryBear);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Stain the Mind");
    }

    @Test
    @DisplayName("May exile only some copies without affecting the battlefield or other player")
    void mayExileOnlySomeMatchingCards() {
        Card handBear = new RuneclawBear();
        Card graveyardBear = new RuneclawBear();
        Card libraryBear = new RuneclawBear();
        Card ownBear = new RuneclawBear();
        harness.setHand(player2, List.of(handBear));
        harness.setGraveyard(player2, List.of(graveyardBear));
        harness.setLibrary(player2, List.of(libraryBear));
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new StainTheMind(), ownBear));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Runeclaw Bear");
        harness.handleMultipleCardsChosen(player1, List.of(graveyardBear.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardBear);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handBear);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryBear);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownBear);
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Can target its controller and name itself without exiling the resolving spell")
    void canTargetControllerAndNameItself() {
        Card otherCopy = new StainTheMind();
        harness.setHand(player1, List.of(new StainTheMind(), otherCopy));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Stain the Mind");
        harness.handleMultipleCardsChosen(player1, List.of(otherCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(otherCopy);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Stain the Mind");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose a land card name")
    void cannotChooseLandName() {
        harness.setHand(player2, List.of(new Swamp()));
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Swamp"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Runeclaw Bear");
        harness.assertInHand(player2, "Swamp");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Black creatures can convoke the black cost even with summoning sickness")
    void canConvokeEntireCostWithSummoningSickCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new RuneclawBear());
        }
        harness.addToBattlefield(player1, new BlackCat());
        List<Permanent> helpers = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        helpers.forEach(p -> p.setSummoningSick(true));
        harness.setHand(player1, List.of(new StainTheMind()));

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                helpers.stream().map(Permanent::getId).toList());

        assertThat(gd.stack).hasSize(1);
        assertThat(helpers).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Already tapped creatures cannot convoke")
    void cannotConvokeWithAlreadyTappedCreature() {
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        helper.tap();
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(player2.getId()), List.of(helper.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stain the Mind");
    }

    @Test
    @DisplayName("Green creatures cannot convoke the required black mana")
    void cannotConvokeBlackCostWithGreenCreatures() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new RuneclawBear());
        }
        List<Permanent> helpers = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        harness.setHand(player1, List.of(new StainTheMind()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(player2.getId()), helpers.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stain the Mind");
    }
}
