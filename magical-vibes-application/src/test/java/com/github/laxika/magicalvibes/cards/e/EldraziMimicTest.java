package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({EldraziMimic.class, Ornithopter.class, GrizzlyBears.class})
class EldraziMimicTest extends BaseCardTest {

    @Test
    @DisplayName("A colorless creature entering can set the Mimic's base power and toughness")
    void colorlessCreatureEnteringSetsBasePowerAndToughnessOnAccept() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Mimic's trigger is optional")
    void colorlessCreatureEnteringCanBeDeclined() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }

    @Test
    @DisplayName("A colored creature entering does not trigger the Mimic")
    void coloredCreatureEnteringDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary base power and toughness change wears off at end of turn")
    void basePowerAndToughnessChangeWearsOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }
}
