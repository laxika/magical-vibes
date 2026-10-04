package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Halfdane.class, AzureDrake.class, Karakas.class})
class HalfdaneTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger targets another creature and copies its current power and toughness")
    void targetsAnotherCreatureAndCopiesCurrentStats() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player1, new AzureDrake());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(drake.getId())
                .doesNotContain(halfdane.getId());

        harness.handlePermanentChosen(player1, drake.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);

        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);
    }

    @Test
    @DisplayName("Changed base power and toughness last through the end of the next upkeep")
    void lastsThroughEndOfNextUpkeep() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player1, new AzureDrake());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, drake.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, drake));
        advanceToUpkeep(player1);

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies the target's power and toughness when the trigger resolves")
    void usesStatsAtResolution() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player2, new AzureDrake());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, drake.getId());
        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(6);
    }

    @Test
    @DisplayName("Halfdane's own counters apply on top of its changed base stats")
    void retainsOwnCounterBonuses() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player2, new AzureDrake());
        halfdane.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, drake.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(5);
    }

    @Test
    @DisplayName("Changed stats persist through the opponent's upkeep without triggering")
    void persistsThroughOpponentUpkeep() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player2, new AzureDrake());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, drake.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(4);
    }

    @Test
    @DisplayName("An ability whose target leaves the battlefield does not change Halfdane")
    void targetLeavingBeforeResolutionDoesNotChangeStats() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player2, new AzureDrake());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, drake.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, drake));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No target can be chosen when Halfdane is the only creature")
    void noLegalTargetWhenAlone() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target an opponent's creature but not a noncreature permanent")
    void targetsOpponentCreatureButNotNoncreature() {
        Permanent halfdane = addCreatureReady(player1, new Halfdane());
        Permanent drake = addCreatureReady(player2, new AzureDrake());
        Permanent karakas = harness.addToBattlefieldAndReturn(player2, new Karakas());
        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(drake.getId())
                .doesNotContain(halfdane.getId(), karakas.getId());

        harness.handlePermanentChosen(player1, drake.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, halfdane)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, halfdane)).isEqualTo(5);
    }
}
