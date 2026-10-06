package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RottingRats.class, GrizzlyBears.class, Unsummon.class})
@DisplayName("Rotting Rats")
class RottingRatsTest extends BaseCardTest {

    @Test
    @DisplayName("On enter, each player discards a card (APNAP: caster first)")
    void eachPlayerDiscardsOnEnter() {
        harness.setHand(player1, new ArrayList<>(List.of(new RottingRats(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // creature enters, ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Active player (player1) discards first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        // Then the opponent discards.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Unearth returns Rotting Rats to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new RottingRats()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve unearth → creature enters, ETB triggers
        harness.passBothPriorities(); // resolve ETB trigger

        // Resolve the ETB discards so the game is left in a clean state.
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        Permanent perm = findPermanent(player1, "Rotting Rats");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Rotting Rats");
    }

    @Test
    @DisplayName("Unearthed Rotting Rats is exiled at the next end step")
    void unearthExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new RottingRats()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve unearth → creature enters, ETB triggers
        harness.passBothPriorities(); // resolve ETB trigger
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rotting Rats");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rotting Rats"));
    }

    @Test
    @DisplayName("Each player's discard happens only after all players have chosen")
    void discardsHappenSimultaneously() {
        harness.setHand(player1, List.of(new RottingRats(), new RottingRats()));
        harness.setHand(player2, List.of(new RottingRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player1, "Rotting Rats");
        harness.assertInGraveyard(player2, "Rotting Rats");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty controller hand does not prevent the opponent from discarding")
    void skipsEmptyControllerHand() {
        harness.setHand(player1, List.of(new RottingRats()));
        harness.setHand(player2, List.of(new RottingRats(), new RottingRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Rotting Rats");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unearth resolves with both hands empty and affects only its source card")
    void unearthWithEmptyHandsReturnsOnlySource() {
        RottingRats source = new RottingRats();
        RottingRats other = new RottingRats();
        harness.setGraveyard(player1, List.of(source, other));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rotting Rats").getCard().getId()).isEqualTo(source.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new RottingRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Rotting Rats");
        harness.assertNotOnBattlefield(player1, "Rotting Rats");
    }

    @Test
    @DisplayName("Returning an unearthed Rats to hand exiles it instead")
    void unearthExilesInsteadOfReturningToHand() {
        harness.setGraveyard(player1, List.of(new RottingRats()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Rotting Rats"));

        harness.assertNotOnBattlefield(player1, "Rotting Rats");
        harness.assertNotInHand(player1, "Rotting Rats");
        harness.assertNotInGraveyard(player1, "Rotting Rats");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rotting Rats"));
    }
}
