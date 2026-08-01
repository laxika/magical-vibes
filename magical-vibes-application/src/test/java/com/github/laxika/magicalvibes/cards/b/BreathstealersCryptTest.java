package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BreathstealersCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a creature prompts pay 3 life or discard; paying keeps the card")
    void payLifeToKeepDrawnCreature() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the payment discards the drawn creature")
    void declineDiscardsDrawnCreature() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-creature draws are revealed but kept with no payment prompt")
    void nonCreatureIsKeptWithoutPrompt() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can't pay 3 life auto-discards the drawn creature with no prompt")
    void cannotPayAutoDiscards() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 2);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's draws are also revealed and subject to the creature discard")
    void affectsOpponentDraws() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
