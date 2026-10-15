package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.c.ClawingTorment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UpriserRenegade.class, GrizzlyBears.class, Plains.class,
        AncestralKatana.class, ClawingTorment.class})
class UpriserRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 for each other modified creature you control")
    void getsPowerForEachOtherModifiedCreatureYouControl() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        Permanent modifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondModifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent modifiedLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingModifiedBear = addCreatureReady(player2, new GrizzlyBears());
        upriser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondModifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedLand.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingModifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, unmodifiedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a bonus without another modified creature you control")
    void doesNotGetBonusWithoutAnotherModifiedCreatureYouControl() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        upriser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts an equipped creature regardless of who controls the Equipment")
    void countsCreatureWithOpponentsEquipment() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        Permanent other = addCreatureReady(player1, new UpriserRenegade());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        equipment.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, upriser)).isEqualTo(3);

        equipment.setAttachedTo(upriser.getId());

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only an Aura controlled by the creature's controller modifies it")
    void distinguishesFriendlyAndOpposingAuras() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        Permanent other = addCreatureReady(player1, new UpriserRenegade());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new ClawingTorment());
        opposingAura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(1);

        opposingAura.setAttachedTo(null);
        Permanent friendlyAura = harness.addToBattlefieldAndReturn(player1, new ClawingTorment());
        friendlyAura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, upriser)).isEqualTo(3);

        friendlyAura.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a creature once even with several modifications and updates when they disappear")
    void countsCreaturesRatherThanModifications() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        Permanent other = addCreatureReady(player1, new UpriserRenegade());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AncestralKatana());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ClawingTorment());
        equipment.setAttachedTo(other.getId());
        aura.setAttachedTo(other.getId());
        other.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, upriser)).isEqualTo(3);

        equipment.setAttachedTo(null);
        aura.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(3);

        other.setCounterCount(CounterType.CHARGE, 0);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(1);
    }
}
