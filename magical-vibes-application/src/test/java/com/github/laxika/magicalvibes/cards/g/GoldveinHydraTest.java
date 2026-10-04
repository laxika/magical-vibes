package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldveinHydra.class, WrathOfGod.class, GiantGrowth.class})
class GoldveinHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new GoldveinHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Goldvein Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates tapped Treasures equal to its power when it dies")
    void deathCreatesTappedTreasuresEqualToPower() {
        harness.addToBattlefieldAndReturn(player1, new GoldveinHydra())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        destroyAllCreatures();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(3);
        assertThat(treasures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Casting with zero X dies and creates no Treasures")
    void zeroXDiesWithoutCreatingTreasures() {
        harness.setHand(player1, List.of(new GoldveinHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Goldvein Hydra");
        harness.assertInGraveyard(player1, "Goldvein Hydra");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Death counts boosted power rather than just counters")
    void deathUsesBoostedLastKnownPower() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new GoldveinHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        destroyAllCreatures();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(5).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An opposing Hydra creates Treasures for its own controller")
    void opposingHydraCreatesTreasuresForItsController() {
        harness.addToBattlefieldAndReturn(player2, new GoldveinHydra())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyAllCreatures();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(2).allMatch(Permanent::isTapped);
    }

    private void destroyAllCreatures() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
