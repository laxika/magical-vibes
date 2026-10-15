package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeluxeDragster.class, GrizzlyBears.class,
        Shock.class, CounselOfTheSoratami.class, Cancel.class})
class DeluxeDragsterTest extends BaseCardTest {

    @Test
    @DisplayName("Can only be blocked by Vehicles")
    void canOnlyBeBlockedByVehicles() {
        Permanent dragster = addCreatureReady(player1, new DeluxeDragster());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        dragster.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vehicles");
    }

    @Test
    @DisplayName("Can be blocked by a Vehicle")
    void canBeBlockedByVehicle() {
        Permanent dragster = addCreatureReady(player1, new DeluxeDragster());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        dragster.setAttacking(true);

        Permanent dreadnought = addCreatureReady(player2, new DeluxeDragster());
        addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dreadnought.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage offers only instants and sorceries from the damaged player's graveyard")
    void combatDamageTargetsOpponentGraveyard() {
        Card shock = new Shock();
        Card counsel = new CounselOfTheSoratami();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(shock, counsel, creature));

        Permanent dragster = addCreatureReady(player1, new DeluxeDragster());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        dragster.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), counsel.getId());
    }

    @Test
    @DisplayName("The chosen spell is cast for free and exiled")
    void castsChosenSpellForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        addCreatureReady(player2, new GrizzlyBears());

        Permanent dragster = addCreatureReady(player1, new DeluxeDragster());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        dragster.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Declining to cast leaves the chosen card in its owner's graveyard")
    void decliningCastLeavesCardInGraveyard() {
        Shock shock = new Shock();
        triggerWithGraveyardCard(shock);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Combat damage cannot choose a spell from the controller's graveyard")
    void excludesControllersGraveyard() {
        Shock ownShock = new Shock();
        Shock opposingShock = new Shock();
        harness.setGraveyard(player1, List.of(ownShock));
        triggerWithGraveyardCard(opposingShock);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opposingShock.getId());
    }

    @Test
    @DisplayName("Crew requires sufficient untapped creature power and taps the crew")
    void crewRequiresPowerAndTapsCreature() {
        Permanent dragster = addCreatureReady(player1, new DeluxeDragster());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(dragster.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, dragster)).isTrue();
    }

    @Test
    @DisplayName("A sorcery can be cast during combat for free and draws cards for the caster")
    void castsSorceryDuringCombat() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, new GrizzlyBears()));
        triggerWithGraveyardCard(counsel);

        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(counsel);
    }

    @Test
    @DisplayName("A chosen card that leaves the graveyard cannot be cast")
    void targetLeavingGraveyardStopsCast() {
        Shock shock = new Shock();
        triggerWithGraveyardCard(shock);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player2, List.of());
        gd.addToExile(player2.getId(), shock);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
    }

    @Test
    @DisplayName("An uncastable counterspell stays in the graveyard rather than being exiled")
    void spellWithoutLegalTargetsStaysInGraveyard() {
        Cancel cancel = new Cancel();
        triggerWithGraveyardCard(cancel);

        harness.handleMultipleCardsChosen(player1, List.of(cancel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(cancel);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(cancel);
        assertThat(gd.stack).isEmpty();
    }

    private void triggerWithGraveyardCard(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent dragster = addCreatureReady(player1, new DeluxeDragster());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        dragster.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
