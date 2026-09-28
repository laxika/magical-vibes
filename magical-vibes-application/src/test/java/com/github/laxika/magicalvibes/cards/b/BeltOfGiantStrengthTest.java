package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeltOfGiantStrength.class, GrizzlyBears.class})
class BeltOfGiantStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has base power and toughness 10/10")
    void setsEquippedCreatureBasePowerAndToughness() {
        Permanent creature = addCreatureReady(player1);
        Permanent belt = addBeltReady(player1);
        belt.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    @DisplayName("Belt does not affect an unattached creature")
    void doesNothingWhileUnattached() {
        Permanent creature = addCreatureReady(player1);
        addBeltReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip costs {8} when targeting a 2-power creature")
    void reducesEquipCostByTargetPower() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addBeltReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BeltOfGiantStrength());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
