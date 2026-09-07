package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonScarredBear.class, GrizzlyBears.class})
class DragonScarredBearTest extends BaseCardTest {

    @Test
    @DisplayName("Formidable regeneration ability grants a regeneration shield")
    void formidableRegenerationGrantsShield() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addGrizzlyBears(player1, 3);
        addRegenerationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration ability can be activated at exactly eight total power")
    void canActivateAtExactThreshold() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addGrizzlyBears(player1, 3);
        bear.setPowerModifier(-1);
        addRegenerationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability cannot be activated below eight total power")
    void cannotActivateBelowThreshold() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addGrizzlyBears(player1, 2);
        addRegenerationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(bear.getRegenerationShield()).isZero();
    }

    private void addGrizzlyBears(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new GrizzlyBears());
        }
    }

    private void addRegenerationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
