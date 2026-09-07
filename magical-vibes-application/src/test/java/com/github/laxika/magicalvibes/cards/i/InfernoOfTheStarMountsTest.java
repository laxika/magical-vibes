package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InfernoOfTheStarMounts.class)
class InfernoOfTheStarMountsTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability is untargeted and boosts Inferno")
    void activatedAbilityBoostsInfernoWithoutTargeting() {
        Permanent inferno = addReadyInferno();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The linked ability triggers only when the boost makes power exactly 20")
    void linkedAbilityTriggersAtExactPowerThreshold() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(13);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("The linked ability does not trigger when power becomes greater than 20")
    void linkedAbilityDoesNotTriggerPastExactPowerThreshold() {
        Permanent inferno = addReadyInferno();
        inferno.setPowerModifier(14);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inferno)).isEqualTo(21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyInferno() {
        Permanent inferno = harness.addToBattlefieldAndReturn(player1, new InfernoOfTheStarMounts());
        inferno.setSummoningSick(false);
        return inferno;
    }
}
