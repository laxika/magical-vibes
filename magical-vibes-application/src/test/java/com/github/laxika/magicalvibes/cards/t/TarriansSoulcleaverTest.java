package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TarriansSoulcleaver.class, GrizzlyBears.class, AngelsFeather.class, Forest.class, MishrasFactory.class})
class TarriansSoulcleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the equipped creature for another artifact or creature")
    void putsCounterForArtifactOrCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = addCreatureReady(player1, new AngelsFeather());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(artifact);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a land")
    void doesNotTriggerForLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());
        Permanent land = addCreatureReady(player2, new Forest());

        putIntoGraveyard(land);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Equip {2} attaches Tarrian's Soulcleaver to a creature you control")
    void equipAttachesSoulcleaver() {
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(soulcleaver.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void countersRemainOnCreatureWhenEquipmentMoves() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(first.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        putIntoGraveyard(victim);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void pendingTriggerUsesLastAttachmentAfterEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, victim));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, soulcleaver));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void unattachedEquipmentDoesNotPutCountersOnOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new TarriansSoulcleaver());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        putIntoGraveyard(victim);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void animatedArtifactCreatureLandDeathTriggersExactlyOnce() {
        Permanent factory = addCreatureReady(player1, new MishrasFactory());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addCreatureReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, factory)).isTrue();
        assertThat(gqs.isArtifact(factory)).isTrue();

        putIntoGraveyard(factory);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
