package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildfireEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R} pumps Wildfire Emissary by +1/+0")
    void pumpAbilityBoostsPower() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(emissary.getPowerModifier()).isEqualTo(1);
        assertThat(emissary.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The pump ability stacks when activated twice")
    void pumpAbilityStacks() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(emissary.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the pump ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player2, new WildfireEmissary());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, emissary.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by a non-white spell")
    void canBeTargetedByNonWhiteSpell() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, emissary.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }
}
