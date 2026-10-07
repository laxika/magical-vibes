package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaskmasterMercenaryMimic.class, GrizzlyBears.class, HydraulicHelper.class})
class TaskmasterMercenaryMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Offers battlefield creatures and creature cards in graveyards")
    void offersBothCreatureZones() {
        addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent battlefieldCreature = addCreatureReady(player2, new GrizzlyBears());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));

        beginFirstMainPhase(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(battlefieldCreature.getId());
        assertThat(choice.validCardIds()).containsExactly(graveyardCreature.getId());
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
        Card original = taskmaster.getCard();
        addCreatureReady(player2, new GrizzlyBears());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(taskmaster.getCard()).isSameAs(original);
    }

    @Test
    void battlefieldCopyHasExactlyTaskmastersCreatureTypes() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent target = addCreatureReady(player2, new HydraulicHelper());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(taskmaster.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.HUMAN, CardSubtype.MERCENARY, CardSubtype.VILLAIN);
    }

    @Test
    void graveyardCopyHasExactlyTaskmastersCreatureTypes() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Card target = new HydraulicHelper();
        harness.setGraveyard(player1, List.of(target));

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(taskmaster.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.HUMAN, CardSubtype.MERCENARY, CardSubtype.VILLAIN);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    void copyingArtifactCreatureReplacesItsCardTypesAndCopiesItsKeyword() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent target = addCreatureReady(player2, new HydraulicHelper());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(taskmaster.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(taskmaster.getCard().hasType(CardType.ARTIFACT)).isFalse();
        assertThat(taskmaster.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(taskmaster.getCard().getPower()).isEqualTo(2);
        assertThat(taskmaster.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void copyLastsThroughOpponentsTurnAndExpiresAtStartOfNextTurn() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent target = addCreatureReady(player2, new HydraulicHelper());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(taskmaster.getCard().getToughness()).isEqualTo(3);
        assertThat(taskmaster.getCard().getKeywords()).contains(Keyword.DEFENDER);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(taskmaster.getCard().getToughness()).isEqualTo(5);
        assertThat(taskmaster.getCard().getKeywords()).doesNotContain(Keyword.DEFENDER);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
    }

    @Test
    void illegalBattlefieldTargetDoesNotChangeTaskmaster() {
        Permanent taskmaster = addCreatureReady(player1, new TaskmasterMercenaryMimic());
        Permanent target = addCreatureReady(player2, new HydraulicHelper());

        beginFirstMainPhase(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(taskmaster.getCard().getToughness()).isEqualTo(5);
        assertThat(taskmaster.getCard().getKeywords()).doesNotContain(Keyword.DEFENDER);
    }

    @Test
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        addCreatureReady(player1, new TaskmasterMercenaryMimic());
        addCreatureReady(player2, new HydraulicHelper());

        beginFirstMainPhase(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void beginFirstMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
