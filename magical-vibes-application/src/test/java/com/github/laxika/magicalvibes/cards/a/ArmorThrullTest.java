package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmorThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Armor Thrull puts a +1/+2 counter on target creature")
    void sacrificesAndPlacesCounter() {
        Permanent armorThrull = addReadyArmorThrull();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(armorThrull).isNotIn(harness.getGameData().playerBattlefields.get(player1.getId()));
        harness.assertInGraveyard(player1, "Armor Thrull");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyArmorThrull();
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyArmorThrull() {
        Permanent armorThrull = new Permanent(new ArmorThrull());
        armorThrull.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(armorThrull);
        return armorThrull;
    }
}
