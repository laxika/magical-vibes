package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdornedPouncer;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VizierOfTheAnointedTest extends BaseCardTest {

    // ── ETB: "you may search your library for a creature card with eternalize or embalm" ──

    @Test
    @DisplayName("ETB search offers only creature cards with eternalize or embalm")
    void etbSearchOffersOnlyEternalizeOrEmbalmCreatures() {
        castVizier();
        harness.setLibrary(player1, List.of(new AdornedPouncer(), new GrizzlyBears()));

        harness.passBothPriorities(); // resolve creature spell -> MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> library search inline

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Adorned Pouncer");
    }

    @Test
    @DisplayName("Choosing a searched card puts it into the graveyard")
    void chosenCardGoesToGraveyard() {
        castVizier();
        harness.setLibrary(player1, List.of(new AdornedPouncer(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Adorned Pouncer"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may search leaves the graveyard empty")
    void decliningSearchDoesNothing() {
        castVizier();
        harness.setLibrary(player1, List.of(new AdornedPouncer(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    // ── Trigger: "whenever you activate an eternalize or embalm ability, draw a card" ──

    @Test
    @DisplayName("Activating an eternalize ability draws a card")
    void eternalizeActivationDrawsCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player1, List.of(new AdornedPouncer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0); // Eternalize {3}{W}{W}
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Activating a non-eternalize/embalm graveyard ability does not draw")
    void nonEternalizeGraveyardAbilityDoesNotDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player1, List.of(new DregscapeZombie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0); // Unearth {B}
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's eternalize activation does not draw for the Vizier's controller")
    void opponentEternalizeDoesNotDraw() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player2, List.of(new AdornedPouncer()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castVizier() {
        harness.setHand(player1, List.of(new VizierOfTheAnointed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
