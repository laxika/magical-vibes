package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalOffering.class, LlanowarElves.class, Island.class})
class InfernalOfferingTest extends BaseCardTest {

    @Test
    void sacrificesCreaturesAndBothPlayersDrawTwo() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(2);

        returnCreature(player1);
        returnCreature(player2);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void onlyPlayersWhoSacrificedDrawTwo() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        castOffering();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).isEmpty();

        returnCreature(player1);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnsOneCreatureFromEachGraveyardInOrder() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new Island()));
        harness.setGraveyard(player2, List.of(new LlanowarElves(), new Island()));
        castOffering();

        PendingInteraction.GraveyardChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, firstChoice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");

        PendingInteraction.GraveyardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player2, secondChoice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    void opponentStillReturnsCreatureWhenControllerHasNoCreatureCards() {
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        castOffering();

        returnCreature(player2);

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void onlyOpponentSacrificesAndDrawsWhenControllerHasNoCreatures() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(2);

        returnCreature(player2);
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void eachPlayerChoosesOneCreatureAndSacrificesHappenTogether() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        var controllerCreatureId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        var opponentCreatureId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        var controllerCardId = gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getId();
        var opponentCardId = gd.playerBattlefields.get(player2.getId()).getFirst().getCard().getId();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        harness.handlePermanentChosen(player1, controllerCreatureId);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handlePermanentChosen(player2, opponentCreatureId);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(controllerCardId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentCardId));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        returnCreature(player1);
        returnCreature(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    void resolvesWithoutDrawingWhenNeitherPlayerHasCreaturesOrCreatureCards() {
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Infernal Offering");
    }

    private void returnCreature(Player player) {
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        harness.handleGraveyardCardChosen(player, choice.validIndices().getFirst());
    }

    private void castOffering() {
        harness.setHand(player1, List.of(new InfernalOffering()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
