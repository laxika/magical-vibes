package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.g.GoblinTombRaider;
import com.github.laxika.magicalvibes.cards.r.RampagingCeratops;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BelligerentYearling.class, RampagingCeratops.class, GoblinTombRaider.class, Abrade.class})
class BelligerentYearlingTest extends BaseCardTest {

    @Test
    @DisplayName("A Dinosaur entering can set the Yearling's base power to its power")
    void dinosaurEnteringSetsBasePowerOnAccept() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.castFromHand(player1, new RampagingCeratops(), "{4}{R}");

        harness.passBothPriorities();
        Permanent enteringDinosaur = findPermanent(player1, "Rampaging Ceratops");
        enteringDinosaur.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(2);

    }

    @Test
    @DisplayName("The Dinosaur trigger is optional")
    void dinosaurEnteringCanBeDeclined() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.castFromHand(player1, new RampagingCeratops(), "{4}{R}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Dinosaur entering does not trigger the ability")
    void nonDinosaurEnteringDoesNotTrigger() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.castFromHand(player1, new GoblinTombRaider(), "{R}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }

    @Test
    @DisplayName("The temporary base-power change wears off at end of turn")
    void basePowerChangeWearsOffAtEndOfTurn() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.castFromHand(player1, new RampagingCeratops(), "{4}{R}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Dinosaur with negative power sets a negative base power")
    void copiesNegativePower() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.castFromHand(player1, new RampagingCeratops(), "{4}{R}");
        harness.passBothPriorities();
        Permanent dinosaur = findPermanent(player1, "Rampaging Ceratops");
        dinosaur.setPowerModifier(-7);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Dinosaur that dies before resolution supplies its last known power")
    void usesLastKnownPowerOfDepartedDinosaur() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.castFromHand(player1, new RampagingCeratops(), "{4}{R}");
        harness.passBothPriorities();
        Permanent dinosaur = findPermanent(player1, "Rampaging Ceratops");
        dinosaur.setPowerModifier(1);
        dinosaur.setToughnessModifier(-2);
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, 0, dinosaur.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rampaging Ceratops");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Yearling does not trigger for its own entrance")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new BelligerentYearling(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opposing Dinosaur does not trigger the Yearling")
    void doesNotTriggerForOpponentsDinosaur() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new RampagingCeratops(), "{4}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Setting base power preserves the Yearling's counters and modifiers")
    void preservesOwnPowerBonuses() {
        Permanent yearling = addCreatureReady(player1, new BelligerentYearling());
        yearling.setPowerModifier(2);
        yearling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castFromHand(player1, new RampagingCeratops(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, yearling)).isEqualTo(3);

        findPermanent(player1, "Rampaging Ceratops").setPowerModifier(2);
        assertThat(gqs.getEffectivePower(gd, yearling)).isEqualTo(8);
    }
}
