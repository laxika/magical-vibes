package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenTrooper.class, GrizzlyBears.class})
class AvenTrooperTest extends BaseCardTest {

    @Test
    void discardingACardAndPayingManaBoostsAvenTrooper() {
        Permanent trooper = addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        prepareAbilityActivation();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent trooper = addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        prepareAbilityActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(1);
    }

    private void prepareAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
