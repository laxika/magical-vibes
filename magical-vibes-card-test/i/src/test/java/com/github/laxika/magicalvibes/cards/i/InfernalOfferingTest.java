package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalOffering.class, LlanowarElves.class, Island.class})
class InfernalOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Both players sacrifice their creature, both draw two, then each returns it from the graveyard")
    void sacrificesCreaturesAndBothPlayersDrawTwo() {
        Permanent ownElves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentElves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownElves);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentElves);
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only a player who sacrificed a creature draws two cards")
    void onlyPlayersWhoSacrificedDrawTwo() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        castOffering();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elves);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).isEmpty();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller returns a creature card first, then the chosen opponent returns one")
    void returnsOneCreatureFromEachGraveyardInOrder() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new LlanowarElves(), new Island()));
        harness.setGraveyard(player2, List.of(new LlanowarElves(), new LlanowarElves(), new Island()));
        castOffering();

        PendingInteraction.GraveyardChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIndices()).containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player1, firstChoice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");

        PendingInteraction.GraveyardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIndices()).containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player2, secondChoice.validIndices().getFirst());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("A lone creature card in a graveyard is returned without a prompt")
    void opponentStillReturnsCreatureWhenControllerHasNoCreatureCards() {
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        castOffering();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the opponent sacrifices and draws when the controller has no creatures")
    void onlyOpponentSacrificesAndDrawsWhenControllerHasNoCreatures() {
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elves);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Each player chooses a creature in turn order, then both are sacrificed together")
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

        harness.handleMultiplePermanentsChosen(player1, List.of(controllerCreatureId));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentCreatureId));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .anyMatch(permanent -> permanent.getCard().getId().equals(controllerCardId));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentCardId));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(controllerCreatureId));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(opponentCreatureId));
    }

    @Test
    @DisplayName("Resolves without drawing or returning when no player has creatures or creature cards")
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

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The death replacement is asked before either creature is sacrificed, and the draws come after")
    void asksTheDeathReplacementBeforeTheSacrificesAndDrawsAfter() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering();

        harness.handleMultiplePermanentsChosen(player1, List.of(elves.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elves);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(zombie);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elves);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(zombie);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2)
                .anyMatch(card -> card.getId().equals(zombie.getCard().getId()));
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
