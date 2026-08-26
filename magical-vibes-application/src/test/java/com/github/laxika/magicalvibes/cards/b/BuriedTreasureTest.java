package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuriedTreasure.class, Forest.class, GrizzlyBears.class})
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

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
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
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1));

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

    private void prepareGraveyardAbility(Card... library) {
        harness.setGraveyard(player1, List.of(new BuriedTreasure()));
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
