package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
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

@CardUsed({BelligerentYearling.class, PygmyAllosaurus.class, GrizzlyBears.class})
class BelligerentYearlingTest extends BaseCardTest {

    @Test
    @DisplayName("A Dinosaur entering can set the Yearling's base power to its power")
    void dinosaurEnteringSetsBasePowerOnAccept() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        PygmyAllosaurus dinosaur = new PygmyAllosaurus();
        dinosaur.setPower(4);
        harness.setHand(player1, List.of(dinosaur));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        Permanent enteringDinosaur = findPermanent(player1, "Pygmy Allosaurus");
        enteringDinosaur.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Dinosaur trigger is optional")
    void dinosaurEnteringCanBeDeclined() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.setHand(player1, List.of(new PygmyAllosaurus()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Dinosaur entering does not trigger the ability")
    void nonDinosaurEnteringDoesNotTrigger() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }

    @Test
    @DisplayName("The temporary base-power change wears off at end of turn")
    void basePowerChangeWearsOffAtEndOfTurn() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.setHand(player1, List.of(new PygmyAllosaurus()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }
}
