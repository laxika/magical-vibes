package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunsetStrikemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sunset Strikemaster adds one red mana")
    void tapsForRedMana() {
        Permanent striker = addCreatureReady(player1, new SunsetStrikemaster());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(striker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The sacrifice ability deals 6 damage to a creature with flying")
    void sacrificesAndDamagesFlyingCreature() {
        addCreatureReady(player1, new SunsetStrikemaster());
        Permanent target = addCreatureReady(player2, new StormtideLeviathan());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Sunset Strikemaster");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addCreatureReady(player1, new SunsetStrikemaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
        harness.assertOnBattlefield(player1, "Sunset Strikemaster");
    }
}
