package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.s.SpatialContortion;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({EldraziMimic.class, Ornithopter.class, GrizzlyBears.class, SpatialContortion.class, PaintersServant.class})
class EldraziMimicTest extends BaseCardTest {

    @Test
    @DisplayName("A colorless creature entering can set the Mimic's base power and toughness")
    void colorlessCreatureEnteringSetsBasePowerAndToughnessOnAccept() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);

        resolveAllTriggers();

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

        resolveAllTriggers();
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

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Mimic does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new EldraziMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eldrazi Mimic");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's colorless creature does not trigger the Mimic")
    void opponentsCreatureDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Mimic uses resolution-time stats and retains its own counters")
    void resolutionTimeStatsIncludeEnteringCountersAndMimicCounters() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        mimic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = findPermanent(player1, "Ornithopter");
        entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(5);

        entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(5);
    }

    @Test
    @DisplayName("A later accepted trigger overwrites the earlier base power and toughness")
    void laterTriggerOverwritesEarlierBaseStats() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new Ornithopter(), new EldraziMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Mimic uses negative last-known toughness when the entering creature dies")
    void enteringCreatureLeavingUsesLastKnownStats() {
        addCreatureReady(player1, new EldraziMimic());
        harness.setHand(player1, List.of(new Ornithopter(), new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = findPermanent(player1, "Ornithopter");
        harness.castAndResolveInstant(player1, 0, entering.getId());
        harness.assertInGraveyard(player1, "Ornithopter");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Eldrazi Mimic");
        harness.assertInGraveyard(player1, "Eldrazi Mimic");
    }

    @Test
    @DisplayName("A creature made colored by Painter's Servant does not trigger the Mimic")
    void enteringCreatureWithGrantedColorDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new EldraziMimic());
        Permanent painter = addCreatureReady(player1, new PaintersServant());
        painter.setChosenColor(CardColor.BLUE);
        harness.setHand(player1, List.of(new EldraziMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof EldraziMimic)
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(1);
    }
}
