package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlueSunsTwilight;
import com.github.laxika.magicalvibes.cards.e.ExperimentalAugury;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeldwebCurator.class, ExperimentalAugury.class, BlueSunsTwilight.class})
class MeldwebCuratorTest extends BaseCardTest {

    private void castCurator() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MeldwebCurator(), "{3}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB offers only an instant or sorcery card and puts the choice on top")
    void putsChosenSpellOnTopOfLibrary() {
        Card filler = new MeldwebCurator();
        Card instant = new ExperimentalAugury();
        Card sorcery = new BlueSunsTwilight();
        Card creature = new MeldwebCurator();
        harness.setLibrary(player1, List.of(filler));
        harness.setGraveyard(player1, List.of(instant, sorcery, creature));

        castCurator();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorcery, filler);
        harness.assertInGraveyard(player1, "Experimental Augury");
        harness.assertInGraveyard(player1, "Meldweb Curator");
    }

    @Test
    @DisplayName("The optional ETB target can be declined")
    void canDeclineTarget() {
        Card instant = new ExperimentalAugury();
        harness.setGraveyard(player1, List.of(instant));

        castCurator();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Experimental Augury");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(instant);
    }

    @Test
    @DisplayName("No graveyard choice is offered when no instant or sorcery is present")
    void ignoresNonSpellCards() {
        Card creature = new MeldwebCurator();
        harness.setGraveyard(player1, List.of(creature));

        castCurator();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Meldweb Curator");
    }

    @Test
    @DisplayName("An instant target is put on top only when the trigger resolves")
    void putsInstantOnTopAtResolution() {
        Card instant = new ExperimentalAugury();
        Card filler = new MeldwebCurator();
        harness.setGraveyard(player1, List.of(instant));
        harness.setLibrary(player1, List.of(filler));

        castCurator();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant, filler);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(instant);
    }

    @Test
    @DisplayName("Only the controller's graveyard supplies targets")
    void excludesOpponentsGraveyard() {
        Card ownInstant = new ExperimentalAugury();
        Card opposingSorcery = new BlueSunsTwilight();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opposingSorcery));

        castCurator();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownInstant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(ownInstant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingSorcery);
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not replaced by another spell")
    void missingTargetDoesNotChooseAnotherCard() {
        Card instant = new ExperimentalAugury();
        Card otherSpell = new BlueSunsTwilight();
        Card filler = new MeldwebCurator();
        harness.setGraveyard(player1, List.of(instant, otherSpell));
        harness.setLibrary(player1, List.of(filler));

        castCurator();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player1, List.of(otherSpell));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherSpell);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
