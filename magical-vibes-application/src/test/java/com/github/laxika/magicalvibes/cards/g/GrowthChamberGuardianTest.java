package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StonyStrength;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowthChamberGuardian.class, Forest.class, StonyStrength.class})
class GrowthChamberGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt 2 puts two +1/+1 counters on Growth-Chamber Guardian and offers a named search")
    void adaptAddsCountersAndOffersSearch() {
        Permanent guardian = addGuardian();
        harness.setLibrary(player1, List.of(new GrowthChamberGuardian(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the named search leaves the library and hand unchanged")
    void decliningSearchDoesNothing() {
        addGuardian();
        harness.setLibrary(player1, List.of(new GrowthChamberGuardian(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Adapt can be activated once Growth-Chamber Guardian has a +1/+1 counter")
    void adaptCanBeActivatedWithCounter() {
        Permanent guardian = addGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two queued adapt activations add counters only once and produce one search")
    void queuedAdaptsCheckCountersAtResolution() {
        Permanent guardian = addGuardian();
        harness.setLibrary(player1, List.of(new GrowthChamberGuardian()));
        addAdaptMana();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A counter from Stony Strength triggers a search even when Guardian already has counters")
    void counterFromAnotherSpellTriggersOnlyItsRecipient() {
        Permanent guardian = addGuardian();
        Permanent otherGuardian = addCreatureReady(player1, new GrowthChamberGuardian());
        addCreatureReady(player2, new GrowthChamberGuardian());
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        guardian.tap();
        harness.setHand(player1, List.of(new StonyStrength()));
        harness.setLibrary(player1, List.of(new GrowthChamberGuardian(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, guardian.getId());
        resolveAllTriggers();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(guardian.isTapped()).isFalse();
        assertThat(otherGuardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Growth-Chamber Guardian");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Accepting a search with no matching Guardian still shuffles and completes")
    void searchWithNoMatchCompletes() {
        addGuardian();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A named search may fail to find even with a matching Guardian in the library")
    void canFailToFindMatchingGuardian() {
        addGuardian();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrowthChamberGuardian(), new Forest()));
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private Permanent addGuardian() {
        return addCreatureReady(player1, new GrowthChamberGuardian());
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
