package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
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

@CardUsed({AmbushWolf.class, LlanowarElves.class, GiantGrowth.class})
class AmbushWolfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts for up to one graveyard target before ability goes on stack")
    void etbPromptsForGraveyardTarget() {
        Card elves = new LlanowarElves();
        harness.setGraveyard(player2, List.of(elves));
        castAmbushWolf();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ambush Wolf");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles the chosen card from a graveyard")
    void etbExilesChosenCard() {
        Card elves = new LlanowarElves();
        harness.setGraveyard(player2, List.of(elves));
        castAmbushWolf();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("ETB can exile a card from its controller's graveyard")
    void etbExilesOwnCard() {
        Card growth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(growth));
        castAmbushWolf();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(growth.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Giant Growth"));
    }

    @Test
    @DisplayName("ETB can choose zero cards")
    void etbCanChooseZeroCards() {
        Card elves = new LlanowarElves();
        harness.setGraveyard(player2, List.of(elves));
        castAmbushWolf();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB with empty graveyards resolves without a target prompt")
    void etbWithEmptyGraveyards() {
        castAmbushWolf();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ambush Wolf");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        castAmbushWolf();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ambush Wolf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Wolf can exile the chosen target before the first trigger resolves")
    void targetExiledInResponseIsNotExiledAgain() {
        Card elves = new LlanowarElves();
        harness.setGraveyard(player2, List.of(elves));
        castAmbushWolf();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        castAmbushWolf();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(elves);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private void castAmbushWolf() {
        harness.setHand(player1, List.of(new AmbushWolf()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }
}
