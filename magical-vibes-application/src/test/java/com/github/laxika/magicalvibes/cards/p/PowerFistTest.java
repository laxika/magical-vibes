package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerFist.class, GrizzlyBears.class})
class PowerFistTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = addPowerFistReady(player1);
        powerFist.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gets combat-damage-scaled +1/+1 counters")
    void combatDamageAddsCountersToEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = addPowerFistReady(player1);
        powerFist.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip ability attaches Power Fist to a creature you control")
    void equipAttachesPowerFist() {
        Permanent powerFist = addPowerFistReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(powerFist.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addPowerFistReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PowerFist());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
