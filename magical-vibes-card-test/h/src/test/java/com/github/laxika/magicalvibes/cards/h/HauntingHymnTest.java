package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntingHymn.class, AshcoatBear.class})
class HauntingHymnTest extends BaseCardTest {

    @Test
    @DisplayName("During the controller's main phase, target player discards four cards")
    void mainPhaseCastDiscardsFour() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new AshcoatBear(), new AshcoatBear(), new AshcoatBear(), new AshcoatBear(), new AshcoatBear())));
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        discardCards(player2, 4);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Outside the controller's main phase, target player discards two cards")
    void nonMainPhaseCastDiscardsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new AshcoatBear(), new AshcoatBear(), new AshcoatBear())));
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        discardCards(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("During an opponent's main phase, target player discards two cards")
    void opponentMainPhaseCastDiscardsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new AshcoatBear(), new AshcoatBear(), new AshcoatBear())));
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        discardCards(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The controller may be chosen as the target")
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new HauntingHymn(), new AshcoatBear(), new AshcoatBear(), new AshcoatBear(), new AshcoatBear())));
        addBlackMana(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        discardCards(player1, 4);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Ashcoat Bear")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("During the controller's second main phase, target player discards four cards")
    void postcombatMainPhaseDiscardsFour() {
        harness.setHand(player2, List.of(
                new AshcoatBear(), new AshcoatBear(), new AshcoatBear(), new AshcoatBear(), new AshcoatBear()));
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        discardCards(player2, 4);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player with fewer than four cards discards their entire hand")
    void shortHandDiscardsAsMuchAsPossible() {
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear(), new AshcoatBear()));
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        discardCards(player2, 3);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting a player with an empty hand resolves without a discard choice")
    void emptyHandDoesNotRequireChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new HauntingHymn()));
        addBlackMana(6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Haunting Hymn");
    }

    private void addBlackMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }

    private void discardCards(Player player, int amount) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < amount; i++) {
            harness.handleCardChosen(player, 0);
        }
    }
}
