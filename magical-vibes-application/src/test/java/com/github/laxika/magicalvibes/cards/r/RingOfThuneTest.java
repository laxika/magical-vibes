package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RingOfThuneTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches the Ring to target creature you control")
    void equipAttachesToCreature() {
        Permanent ring = addRingReady(player1);
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures do not gain vigilance")
    void unattachedRingGrantsNothing() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        addRingReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Upkeep trigger puts a +1/+1 counter on a white equipped creature")
    void upkeepAddsCounterToWhiteCreature() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger does nothing when the equipped creature is not white")
    void upkeepDoesNothingForNonWhiteCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does nothing while the Ring is unattached")
    void upkeepDoesNothingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        addRingReady(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addRingReady(Player player) {
        Permanent perm = new Permanent(new RingOfThune());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
