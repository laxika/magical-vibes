package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
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

@CardUsed({Fleshgrafter.class, AuriokChampion.class, MyrServitor.class})
class FleshgrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding an artifact gives Fleshgrafter +2/+2 until end of turn")
    void discardingArtifactBoostsFleshgrafter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent fleshgrafter = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        int basePower = gqs.getEffectivePower(gd, fleshgrafter);
        int baseToughness = gqs.getEffectiveToughness(gd, fleshgrafter);
        harness.setHand(player1, List.of(new AuriokChampion(), new MyrServitor()));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.DiscardCostChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Myr Servitor");
        assertThat(gqs.getEffectivePower(gd, fleshgrafter)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, fleshgrafter)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent fleshgrafter = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        int basePower = gqs.getEffectivePower(gd, fleshgrafter);
        int baseToughness = gqs.getEffectiveToughness(gd, fleshgrafter);
        harness.setHand(player1, List.of(new MyrServitor()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fleshgrafter)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, fleshgrafter)).isEqualTo(baseToughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fleshgrafter)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, fleshgrafter)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Can activate more than once in the same turn")
    void canActivateMoreThanOncePerTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent fleshgrafter = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        int basePower = gqs.getEffectivePower(gd, fleshgrafter);
        int baseToughness = gqs.getEffectiveToughness(gd, fleshgrafter);
        harness.setHand(player1, List.of(new MyrServitor(), new MyrServitor()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, fleshgrafter)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, fleshgrafter)).isEqualTo(baseToughness + 4);
    }

    @Test
    @DisplayName("Cannot activate without an artifact card to discard")
    void cannotActivateWithoutArtifactInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        harness.setHand(player1, List.of(new AuriokChampion()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
