package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeltOfGiantStrength.class, GrizzlyBears.class, Ornithopter.class})
class BeltOfGiantStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has base power and toughness 10/10")
    void equippedCreatureHasBaseTenTen() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        belt.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    @DisplayName("An unattached Belt does not change a creature's power or toughness")
    void unattachedBeltDoesNotBoostCreature() {
        addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip costs less by the target creature's power")
    void equipCostIsReducedByTargetPower() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip reduction uses power rather than toughness")
    void equipReductionUsesPower() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(belt.getAttachedTo()).isNull();
    }

    private Permanent addBeltReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BeltOfGiantStrength());
        permanent.setSummoningSick(false);
        return permanent;
    }

}
