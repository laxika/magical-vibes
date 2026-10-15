package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaraudingMako;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HostileInvestigator.class, Distress.class, GrizzlyBears.class, MaraudingMako.class, MindRot.class})
class HostileInvestigatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB discard makes Hostile Investigator investigate")
    void etbDiscardInvestigates() {
        harness.setHand(player1, List.of(new HostileInvestigator()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Investigates only once each turn across controller and opponent discards")
    void investigatesOnlyOnceEachTurn() {
        harness.setHand(player1, new ArrayList<>(List.of(new HostileInvestigator(), new Distress())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Controller discarding also makes Hostile Investigator investigate")
    void controllerDiscardInvestigates() {
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.setHand(player1, List.of(new MaraudingMako()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target its controller with the ETB discard")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new HostileInvestigator()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("An opponent with an empty hand does not cause an investigation")
    void emptyOpponentHandDoesNotInvestigate() {
        harness.setHand(player1, List.of(new HostileInvestigator()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hostile Investigator");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Controller and opponent discards share the same once-per-turn limit")
    void controllerDiscardThenOpponentDiscardInvestigatesOnlyOnce() {
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.setHand(player1, List.of(new MaraudingMako(), new Distress()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Each Investigator investigates independently from the same discard")
    void multipleInvestigatorsEachInvestigate() {
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.setHand(player1, List.of(new Distress()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Discarding two cards in one event investigates only once for either player")
    void multipleCardDiscardInvestigatesOnce(boolean controllerDiscards) {
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.setHand(player1, controllerDiscards
                ? List.of(new MindRot(), new GrizzlyBears(), new GrizzlyBears())
                : List.of(new MindRot()));
        harness.setHand(player2, controllerDiscards
                ? List.of() : List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        var discardingPlayer = controllerDiscards ? player1 : player2;

        harness.castAndResolveSorcery(player1, 0, discardingPlayer.getId());
        harness.handleCardChosen(discardingPlayer, 0);
        harness.handleCardChosen(discardingPlayer, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(discardingPlayer.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates again during the opponent's next turn")
    void investigatesAgainOnNextTurn() {
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.setHand(player1, List.of(new MaraudingMako(), new MaraudingMako()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        harness.addToBattlefield(player1, new HostileInvestigator());
        harness.setHand(player1, List.of(new Distress()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
