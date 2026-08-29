package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydrasGrowth.class, GrizzlyBears.class, Demystify.class})
class HydrasGrowthTest extends BaseCardTest {

    @Test
    void entersWithACounterOnTheEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castHydrasGrowth(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doublesEnchantedCreaturesCountersAtControllerUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castHydrasGrowth(creature);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void upkeepTriggerDoesNotFireDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castHydrasGrowth(creature);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersTriggerUsesLastEnchantedCreatureIfAuraLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HydrasGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        UUID auraId = harness.getPermanentId(player1, "Hydra's Growth");
        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, auraId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castHydrasGrowth(Permanent creature) {
        harness.setHand(player1, List.of(new HydrasGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
