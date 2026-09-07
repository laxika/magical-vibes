package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AstralSlide.class, Censor.class, Distress.class, GrizzlyBears.class})
class AstralSlideTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card offers to exile a target creature and returns it at the next end step")
    void cyclingExilesAndReturnsTargetCreature() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        advanceToEndStep(player1);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling by an opponent also triggers Astral Slide")
    void opponentCyclingTriggersSlide() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An ordinary discard does not trigger Astral Slide")
    void ordinaryDiscardDoesNotTriggerSlide() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Distress())));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
