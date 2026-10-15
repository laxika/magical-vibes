package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KittKantoMayhemDiva.class, GrizzlyBears.class})
class KittKantoMayhemDivaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a green and white Citizen token")
    void enteringCreatesCitizenToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new KittKantoMayhemDiva(), "{1}{R}{G}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    @DisplayName("The Citizen created on entry is an untapped green and white 1/1 creature")
    void citizenHasTheRequiredTokenCharacteristics() {
        harness.enterBattlefieldAndReturn(player1, new KittKantoMayhemDiva());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(citizen.getCard().isToken()).isTrue();
        assertThat(citizen.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(citizen.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(1);
        assertThat(citizen.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping two creatures boosts and goads a creature controlled by the active player")
    void tappingTwoCreaturesBoostsAndGoadsActivePlayersCreature() {
        addCreatureReady(player1, new KittKantoMayhemDiva());
        Permanent tapperOne = addCreatureReady(player1, new GrizzlyBears());
        Permanent tapperTwo = addCreatureReady(player1, new GrizzlyBears());
        Permanent activePlayersCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonActivePlayersCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, tapperOne.getId());
        harness.handlePermanentChosen(player1, tapperTwo.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(activePlayersCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonActivePlayersCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, activePlayersCreature.getId());
        harness.passBothPriorities();

        assertThat(tapperOne.isTapped()).isTrue();
        assertThat(tapperTwo.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, activePlayersCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, activePlayersCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, activePlayersCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, activePlayersCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonActivePlayersCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the tap cost leaves the ability's target unchanged")
    void decliningTapCostDoesNothing() {
        addCreatureReady(player1, new KittKantoMayhemDiva());
        Permanent tapperOne = addCreatureReady(player1, new GrizzlyBears());
        Permanent tapperTwo = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tapperOne.isTapped()).isFalse();
        assertThat(tapperTwo.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }

    @Test
    @DisplayName("The tap cost cannot be accepted without two untapped creatures")
    void cannotPayTapCostWithoutTwoUntappedCreatures() {
        addCreatureReady(player1, new KittKantoMayhemDiva());
        Permanent tapper = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        tapper.tap();

        advanceToCombat(player2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tapper.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Paying the tap cost creates a separate trigger before the target is boosted or goaded")
    void tapPaymentLeavesTimeToRespondBeforeBoostAndGoad() {
        addCreatureReady(player1, new KittKantoMayhemDiva());
        Permanent tapperOne = addCreatureReady(player1, new GrizzlyBears());
        Permanent tapperTwo = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, tapperOne.getId());
        harness.handlePermanentChosen(player1, tapperTwo.getId());
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(tapperOne.isTapped()).isTrue();
        assertThat(tapperTwo.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kitt Kanto and a summoning-sick creature can pay the cost on their controller's turn")
    void summoningSickCreaturesCanTapAndTargetKittOnOwnTurn() {
        Permanent kitt = harness.addToBattlefieldAndReturn(player1, new KittKantoMayhemDiva());
        Permanent tapper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, kitt.getId());
        resolveAllTriggers();

        assertThat(kitt.isTapped()).isTrue();
        assertThat(tapper.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, kitt)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kitt)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, kitt, Keyword.TRAMPLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, kitt)).isEqualTo(1);
    }

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
