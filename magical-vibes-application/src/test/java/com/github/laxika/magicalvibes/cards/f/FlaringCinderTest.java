package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlaringCinderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield lets its controller discard to draw")
    void etbLetsControllerDiscardToDraw() {
        setDeck(new Forest());
        harness.setHand(player1, new ArrayList<>(List.of(new FlaringCinder(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater triggers the ability")
    void qualifyingSpellLetsControllerDiscardToDraw() {
        harness.addToBattlefield(player1, new FlaringCinder());
        setDeck(new Forest());
        harness.setHand(player1, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the optional ability neither discards nor draws")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new FlaringCinder());
        setDeck(new Forest());
        harness.setHand(player1, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not trigger the ability")
    void nonqualifyingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FlaringCinder());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void setDeck(Forest card) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(card);
    }
}
