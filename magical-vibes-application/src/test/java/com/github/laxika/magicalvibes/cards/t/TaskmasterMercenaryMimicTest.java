package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaskmasterMercenaryMimic.class, GrizzlyBears.class})
class TaskmasterMercenaryMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Offers battlefield creatures and creature cards in graveyards")
    void offersBothCreatureZones() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent battlefieldCreature = addCreatureReady(player2, new GrizzlyBears());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));

        beginFirstMainPhase(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(battlefieldCreature.getId());
        assertThat(choice.validCardIds()).containsExactly(graveyardCreature.getId());
        assertThat(taskmaster.getCard().getName()).isEqualTo("Taskmaster, Mercenary Mimic");
    }

    @Test
    @DisplayName("Copies a battlefield creature with Taskmaster's exceptions")
    void copiesBattlefieldCreature() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent battlefieldCreature = addCreatureReady(player2, new GrizzlyBears());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(battlefieldCreature.getId()));
        resolveAllTriggers();

        assertThat(taskmaster.getCard().getName()).isEqualTo("Taskmaster, Mercenary Mimic");
        assertThat(taskmaster.getCard().getPower()).isEqualTo(2);
        assertThat(taskmaster.getCard().getToughness()).isEqualTo(2);
        assertThat(taskmaster.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(taskmaster.getCard().getSubtypes())
                .contains(CardSubtype.HUMAN, CardSubtype.VILLAIN);
        assertThat(taskmaster.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Becomes a copy of a selected graveyard creature with Taskmaster's exceptions")
    void copiesGraveyardCreature() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(graveyardCreature.getId()));
        resolveAllTriggers();

        assertThat(taskmaster.getCard().getName()).isEqualTo("Taskmaster, Mercenary Mimic");
        assertThat(taskmaster.getCard().getPower()).isEqualTo(2);
        assertThat(taskmaster.getCard().getToughness()).isEqualTo(2);
        assertThat(taskmaster.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("May choose no creature")
    void mayChooseNoCreature() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        addCreatureReady(player2, new GrizzlyBears());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(taskmaster.getCard().getName()).isEqualTo("Taskmaster, Mercenary Mimic");
    }

    private void beginFirstMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
