package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SpireMechcycle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroodheartEngine.class, GrizzlyBears.class, HolyDay.class, SpireMechcycle.class})
class BroodheartEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of its controller's upkeep")
    void upkeepSurveilsOne() {
        harness.addToBattlefield(player1, new BroodheartEngine());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Sacrificing it returns a creature from the graveyard")
    void sacrificesAndReturnsCreature() {
        addReadyEngine(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Broodheart Engine");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can return a Vehicle from the graveyard")
    void returnsVehicle() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spire Mechcycle");
        harness.assertNotInGraveyard(player1, "Spire Mechcycle");
    }

    @Test
    @DisplayName("The ability cannot choose a noncreature non-Vehicle card")
    void cannotChooseInvalidGraveyardCard() {
        addReadyEngine(player1);
        Card noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target card must be a creature or Vehicle card");
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void canKeepSurveilledCard() {
        harness.addToBattlefield(player1, new BroodheartEngine());
        Card topCard = new SpireMechcycle();
        Card secondCard = new SpireMechcycle();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Does not surveil during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BroodheartEngine());
        Card topCard = new SpireMechcycle();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Surveilling an empty library does not require a choice")
    void surveilsEmptyLibrary() {
        harness.addToBattlefield(player1, new BroodheartEngine());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A noncreature artifact can activate the turn it enters")
    void activatesNewArtifactAndPaysCostsImmediately() {
        harness.addToBattlefield(player1, new BroodheartEngine());
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Broodheart Engine");
        harness.assertInGraveyard(player1, "Broodheart Engine");
        harness.assertNotOnBattlefield(player1, "Spire Mechcycle");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vehicle);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spire Mechcycle");
        assertThat(findPermanent(player1, "Spire Mechcycle").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateTappedArtifact() {
        Permanent engine = addReadyEngine(player1);
        engine.tap();
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player1, "Broodheart Engine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringUpkeep() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Broodheart Engine");
    }

    @Test
    @DisplayName("Cannot return a card from the opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player2, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Broodheart Engine");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(vehicle);
    }

    @Test
    @DisplayName("The sacrificed artifact stays sacrificed when the target leaves the graveyard")
    void targetLeavingGraveyardStopsReturn() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).remove(vehicle);
        harness.setExile(player1, List.of(vehicle));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spire Mechcycle");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(vehicle);
        harness.assertInGraveyard(player1, "Broodheart Engine");
    }

    @Test
    @DisplayName("Cannot activate during the opponent's main phase")
    void cannotActivateOnOpponentsTurn() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        addAbilityMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Broodheart Engine");
    }

    @Test
    @DisplayName("Cannot activate with a spell on the stack")
    void cannotActivateInResponse() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.setHand(player1, List.of(new HolyDay()));
        addAbilityMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Broodheart Engine");
    }

    @Test
    @DisplayName("Cannot sacrifice the artifact without paying the full mana cost")
    void cannotActivateWithoutEnoughMana() {
        addReadyEngine(player1);
        Card vehicle = new SpireMechcycle();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Broodheart Engine");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(vehicle);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    private Permanent addReadyEngine(Player player) {
        return addCreatureReady(player, new BroodheartEngine());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
