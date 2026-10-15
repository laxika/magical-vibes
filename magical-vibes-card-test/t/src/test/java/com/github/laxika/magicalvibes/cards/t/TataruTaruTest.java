package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TataruTaru.class, GrizzlyBears.class, TrueBeliever.class})
class TataruTaruTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, draws for its controller, and offers the opponent a draw")
    void entersDrawsAndOffersOpponentDraw() {
        harness.setHand(player1, List.of(new TataruTaru()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castCreature(player1, 0);
        int player1HandBeforeDraw = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBeforeDraw + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);
    }

    @Test
    @DisplayName("Creates one tapped Treasure for an opponent draw outside that player's turn")
    void createsTappedTreasureOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new TataruTaru());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player2);
        draw(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure"))
                .singleElement()
                .extracting(Permanent::isTapped)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("Does not create a Treasure when the opponent draws during their own turn")
    void doesNotTriggerDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TataruTaru());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The enters ability cannot resolve without a legal opponent target")
    void opponentWithShroudPreventsEntireEntersAbility() {
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.setHand(player1, List.of(new TataruTaru()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Tataru Taru")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent may decline the draw without preventing the controller's draw")
    void opponentDeclinesDraw() {
        harness.setHand(player1, List.of(new TataruTaru()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Controller draws do not trigger or consume the opponent-draw allowance")
    void controllerDrawDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new TataruTaru());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure"))
                .singleElement()
                .extracting(Permanent::isTapped)
                .isEqualTo(true);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
