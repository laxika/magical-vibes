package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CryptLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the sacrifice mode sacrifices a creature and draws a card")
    void sacrificeModeSacrificesCreatureAndDraws() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.addToBattlefield(player1, creature);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new CryptLurker())));
        castCryptLurker();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Sacrifice a creature");

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Accepting the discard mode discards only a creature card and draws a card")
    void discardModeDiscardsCreatureAndDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new CryptLurker(), new Forest(), new GrizzlyBears())));
        castCryptLurker();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a creature card");

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the ETB ability does nothing")
    void decliningDoesNothing() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new CryptLurker(), new GrizzlyBears())));
        castCryptLurker();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castCryptLurker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }
}
