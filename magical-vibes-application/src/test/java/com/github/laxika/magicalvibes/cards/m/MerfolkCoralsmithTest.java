package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkCoralsmith.class, GrizzlyBears.class})
class MerfolkCoralsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives it +1/-1 until end of turn")
    void activationBoostsSelf() {
        Permanent coralsmith = addReadyCoralsmith();
        int basePower = gqs.getEffectivePower(gd, coralsmith);
        int baseToughness = gqs.getEffectiveToughness(gd, coralsmith);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, coralsmith)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, coralsmith)).isEqualTo(baseToughness - 1);
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent coralsmith = addReadyCoralsmith();
        int basePower = gqs.getEffectivePower(gd, coralsmith);
        int baseToughness = gqs.getEffectiveToughness(gd, coralsmith);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, coralsmith)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, coralsmith)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("When it dies, it offers scry 2")
    void diesWithScryTwo() {
        Permanent coralsmith = addReadyCoralsmith();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, coralsmith));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyCoralsmith() {
        Permanent coralsmith = harness.addToBattlefieldAndReturn(player1, new MerfolkCoralsmith());
        coralsmith.setSummoningSick(false);
        return coralsmith;
    }
}
