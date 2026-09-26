package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MoonwingMoth;
import com.github.laxika.magicalvibes.cards.s.Secretkeeper;
import com.github.laxika.magicalvibes.cards.s.SoramaroFirstToDream;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrustedAdvisor.class, MoonwingMoth.class, Secretkeeper.class, SoramaroFirstToDream.class})
class TrustedAdvisorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's maximum hand size is increased by two")
    void controllerMaximumHandSizeIsIncreasedByTwo() {
        harness.addToBattlefield(player1, new TrustedAdvisor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfSize(9));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Controller must discard above the increased maximum hand size")
    void controllerDiscardsAboveIncreasedMaximumHandSize() {
        harness.addToBattlefield(player1, new TrustedAdvisor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfSize(10));

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Trusted Advisor does not increase an opponent's maximum hand size")
    void opponentMaximumHandSizeIsUnaffected() {
        harness.addToBattlefield(player1, new TrustedAdvisor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSize(8));

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Maximum hand size returns to seven when Trusted Advisor leaves")
    void maximumHandSizeReturnsToSevenWhenSourceLeaves() {
        harness.addToBattlefield(player1, new TrustedAdvisor());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfSize(8));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep returns a chosen blue creature the controller controls")
    void upkeepReturnsChosenBlueCreature() {
        Permanent trustedAdvisor = addCreatureReady(player1, new TrustedAdvisor());
        Permanent blueCreature = addCreatureReady(player1, new SoramaroFirstToDream());
        Permanent nonBlueCreature = addCreatureReady(player1, new MoonwingMoth());
        Permanent opponentBlueCreature = addCreatureReady(player2, new SoramaroFirstToDream());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(blueCreature.getId(), trustedAdvisor.getId())
                .doesNotContain(nonBlueCreature.getId(), opponentBlueCreature.getId());

        harness.handlePermanentChosen(player1, blueCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Soramaro, First to Dream");
        harness.assertInHand(player1, "Soramaro, First to Dream");
        harness.assertOnBattlefield(player1, "Trusted Advisor");
        harness.assertOnBattlefield(player1, "Moonwing Moth");
    }

    @Test
    @DisplayName("Upkeep ability does nothing when no blue creature remains")
    void upkeepAbilityDoesNothingWithoutAValidBlueCreature() {
        addCreatureReady(player1, new TrustedAdvisor());
        addCreatureReady(player1, new MoonwingMoth());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returned creature goes to its owner's hand even when controlled by Trusted Advisor's controller")
    void returnedCreatureGoesToItsOwnersHand() {
        addCreatureReady(player1, new TrustedAdvisor());
        Secretkeeper stolenCard = new Secretkeeper();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenBlueCreature = addCreatureReady(player1, stolenCard);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, stolenBlueCreature.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Secretkeeper");
        harness.assertInHand(player2, "Secretkeeper");
    }

    @Test
    @DisplayName("Upkeep ability does not trigger during an opponent's upkeep")
    void upkeepAbilityDoesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new TrustedAdvisor());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private List<Card> handOfSize(int size) {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            hand.add(new MoonwingMoth());
        }
        return hand;
    }
}
