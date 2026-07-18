package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnergyTapTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the creature and adds {C} equal to its mana value")
    void tapsCreatureAndAddsColorlessEqualToManaValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // {1}{G}, MV 2
        harness.setHand(player1, List.of(new EnergyTap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless produced scales with the creature's mana value")
    void manaScalesWithCreatureManaValue() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // {3}{U}{U}, MV 5
        harness.setHand(player1, List.of(new EnergyTap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetCreatureYouDontControl() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // a legal target exists, so the card is playable
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnergyTap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enemy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature you control");
    }

    @Test
    @DisplayName("Cannot target an already-tapped creature")
    void cannotTargetTappedCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // untapped, legal target keeps the card playable
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapped.tap();
        harness.setHand(player1, List.of(new EnergyTap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, tapped.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature you control");
    }
}
