package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MesmericFiend.class, CabalCoffers.class, DeepAnalysis.class, FieryTemper.class,
        SeatOfTheSynod.class})
class MesmericFiendTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MesmericFiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB reveals the opponent's hand and prompts for a nonland card")
    void etbRevealsHandAndPromptsForChoice() {
        harness.setHand(player2, List.of(new DeepAnalysis(), new CabalCoffers(), new FieryTemper()));

        castAndResolveEtb();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 2);
    }

    @Test
    @DisplayName("Choosing a nonland card exiles it")
    void choosingNonlandCardExilesIt() {
        harness.setHand(player2, List.of(new DeepAnalysis(), new CabalCoffers()));

        castAndResolveEtb();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Deep Analysis"));
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Cabal Coffers"));
    }

    @Test
    @DisplayName("The exiled card returns to its owner's hand when Mesmeric Fiend leaves")
    void exiledCardReturnsWhenSourceLeaves() {
        harness.setHand(player2, List.of(new DeepAnalysis()));

        castAndResolveEtb();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        UUID fiendId = harness.getPermanentId(player1, "Mesmeric Fiend");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fiendId);

        harness.assertNotOnBattlefield(player1, "Mesmeric Fiend");
        harness.assertInHand(player2, "Deep Analysis");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Deep Analysis"));
    }

    @Test
    @DisplayName("The ETB ability cannot target its controller")
    void cannotTargetItsController() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MesmericFiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be an opponent");
    }

    @Test
    @DisplayName("No choice is offered when the opponent has no nonland cards")
    void noChoiceWhenOpponentHasOnlyLands() {
        harness.setHand(player2, List.of(new CabalCoffers()));

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class))
                .isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Cabal Coffers");
    }

    @Test
    @DisplayName("An artifact land is not a valid nonland choice")
    void artifactLandCannotBeChosen() {
        harness.setHand(player2, List.of(new SeatOfTheSynod(), new DeepAnalysis()));

        castAndResolveEtb();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
    }
}
