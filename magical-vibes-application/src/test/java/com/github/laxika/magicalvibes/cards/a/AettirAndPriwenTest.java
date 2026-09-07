package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AettirAndPriwen.class, GrizzlyBears.class})
class AettirAndPriwenTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has base power and toughness equal to its Equipment controller's life total")
    void setsEquippedCreatureBasePowerAndToughnessToControllerLife() {
        harness.setLife(player1, 14);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(14);
    }

    @Test
    @DisplayName("Equipped creature's base power and toughness update with life total changes")
    void updatesWhenControllerLifeChanges() {
        harness.setLife(player1, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());
        equipment.setAttachedTo(creature.getId());

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(21);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(21);

        harness.setLife(player1, 8);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    @DisplayName("Unattached Equipment does not set a creature's base power and toughness")
    void doesNothingWhileUnattached() {
        harness.setLife(player1, 14);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
