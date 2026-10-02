package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StonyStrength;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenthicBiomancer.class, Forest.class, StonyStrength.class, BiogenicUpgrade.class})
class BenthicBiomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt puts a +1/+1 counter on Benthic Biomancer and loots")
    void adaptAddsCounterAndLoots() {
        Permanent biomancer = addBiomancer();
        StonyStrength discard = new StonyStrength();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(drawn));
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        int discardIndex = gd.playerHands.get(player1.getId()).indexOf(discard);
        harness.handleCardChosen(player1, discardIndex);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    @DisplayName("Adapt can be activated once Benthic Biomancer has a +1/+1 counter")
    void adaptCanBeActivatedWithCounter() {
        Permanent biomancer = addBiomancer();
        biomancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adapt does not put a second counter when the ability resolves after a counter is added")
    void adaptChecksForCountersOnResolution() {
        Permanent biomancer = addBiomancer();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        biomancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters from another card trigger looting even when a counter is already present")
    void externalCounterTriggersLoot() {
        Permanent biomancer = addBiomancer();
        biomancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Forest kept = new Forest();
        StonyStrength drawn = new StonyStrength();
        harness.setHand(player1, List.of(new StonyStrength(), kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, biomancer.getId());
        resolveAllTriggers();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Looting with an empty hand draws and then discards the drawn card")
    void emptyHandStillDrawsThenDiscards() {
        addBiomancer();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Adapt works while summoning sick and tapped")
    void adaptDoesNotRequireTappingOrHaste() {
        Permanent biomancer = harness.addToBattlefieldAndReturn(player1, new BenthicBiomancer());
        biomancer.setSummoningSick(true);
        biomancer.tap();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(biomancer.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Each simultaneous placement of several counters triggers only one loot")
    void multipleCountersTriggerOncePerPlacement() {
        Permanent biomancer = addBiomancer();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        StonyStrength firstDiscard = new StonyStrength();
        StonyStrength secondDiscard = new StonyStrength();
        harness.setHand(player1, List.of(new BiogenicUpgrade(), firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, Map.of(biomancer.getId(), 3));
        harness.passBothPriorities();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBiomancer() {
        return addCreatureReady(player1, new BenthicBiomancer());
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
