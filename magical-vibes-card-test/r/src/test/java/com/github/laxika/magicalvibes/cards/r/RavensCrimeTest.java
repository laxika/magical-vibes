package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FetidHeath;
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

@CardUsed({RavensCrime.class, FetidHeath.class})
class RavensCrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card of their choice")
    void targetDiscardsOneCard() {
        harness.setHand(player2, List.of(new FetidHeath(), new RavensCrime()));
        harness.setHand(player1, List.of(new RavensCrime()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // The targeted player, not the caster, chooses the discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0); // discard Fetid Heath

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Raven's Crime");
        harness.assertInGraveyard(player2, "Fetid Heath");
    }

    @Test
    @DisplayName("Target player with an empty hand does not choose a discard")
    void emptyTargetHandDoesNotPrompt() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RavensCrime()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Raven's Crime");
    }

    @Test
    @DisplayName("Retrace lets Raven's Crime be recast from the graveyard by discarding a land")
    void retraceFromGraveyard() {
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.setGraveyard(player1, List.of(new RavensCrime()));
        harness.setHand(player1, List.of(new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Recast from graveyard (index 0), discarding the land in hand (index 0), targeting player2.
        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Raven's Crime");
        // The retraced land goes to the graveyard as part of the additional cost.
        harness.assertInGraveyard(player1, "Fetid Heath");
        // Retrace keeps normal graveyard disposition, so Raven's Crime can be retraced again.
        harness.assertInGraveyard(player1, "Raven's Crime");
    }

    @Test
    @DisplayName("Retrace discards the chosen land from a mixed hand")
    void retraceDiscardsChosenLandFromMixedHand() {
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.setGraveyard(player1, List.of(new RavensCrime()));
        harness.setHand(player1, List.of(new RavensCrime(), new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castRetrace(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Raven's Crime");
        harness.assertNotInHand(player1, "Fetid Heath");
        harness.assertInGraveyard(player1, "Fetid Heath");
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new RavensCrime()));
        harness.setHand(player1, List.of(new RavensCrime()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Raven's Crime can target its caster")
    void canTargetCaster() {
        harness.setHand(player1, List.of(new RavensCrime(), new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Fetid Heath");
        harness.assertInGraveyard(player1, "Raven's Crime");
    }

    @Test
    @DisplayName("Retrace pays its land cost before resolution and can be used again")
    void canRetraceRepeatedly() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(new RavensCrime()));
        harness.setHand(player1, List.of(new FetidHeath(), new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castRetrace(player1, 0, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Fetid Heath");
        harness.assertNotInGraveyard(player1, "Raven's Crime");

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Raven's Crime");

        harness.castRetrace(player1, 1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Raven's Crime");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Retrace still requires paying the normal mana cost")
    void retraceRequiresMana() {
        harness.setGraveyard(player1, List.of(new RavensCrime()));
        harness.setHand(player1, List.of(new FetidHeath()));

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fetid Heath");
        harness.assertInGraveyard(player1, "Raven's Crime");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Retrace does not allow Raven's Crime to be cast outside a main phase")
    void retraceRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new RavensCrime()));
        harness.setHand(player1, List.of(new FetidHeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fetid Heath");
        harness.assertInGraveyard(player1, "Raven's Crime");
        assertThat(gd.stack).isEmpty();
    }
}
