package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimeboundDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana grants Rimebound Dead a regeneration shield")
    void snowManaGrantsRegenerationShield() {
        Permanent dead = addRimeboundDeadReady(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addRimeboundDeadReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addRimeboundDeadReady(Player player) {
        Permanent dead = new Permanent(new RimeboundDead());
        dead.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(dead);
        return dead;
    }
}
