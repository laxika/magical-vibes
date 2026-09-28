package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KittKantoMayhemDiva.class, GrizzlyBears.class})
class KittKantoMayhemDivaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a green and white Citizen token")
    void enteringCreatesCitizenToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KittKantoMayhemDiva()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
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

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
