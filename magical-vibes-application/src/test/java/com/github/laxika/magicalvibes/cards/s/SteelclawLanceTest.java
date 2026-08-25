package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelclawLance.class, BenalishKnight.class, GrizzlyBears.class})
class SteelclawLanceTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusTwo() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lance.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void knightEquipAttachesToKnightForOneMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(knight.getId());
    }

    @Test
    void knightEquipRejectsNonKnight() {
        harness.addToBattlefield(player1, new SteelclawLance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Knight");
    }

    @Test
    void genericEquipAttachesToNonKnightForThreeMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(creature.getId());
    }
}
