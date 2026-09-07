package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CauterySliver.class, GrizzlyBears.class})
class CauterySliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver can sacrifice itself to deal 1 damage to a player")
    void sacrificesToDealDamage() {
        Permanent sliver = addCreatureReady(player1, new CauterySliver());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sliver);
    }

    @Test
    @DisplayName("A Sliver can sacrifice itself to prevent the next damage to a player")
    void sacrificesToPreventDamage() {
        addCreatureReady(player1, new CauterySliver());
        addCreatureReady(player1, new CauterySliver());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The prevention ability cannot target a non-Sliver creature")
    void preventionAbilityCannotTargetNonSliverCreature() {
        addCreatureReady(player1, new CauterySliver());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sliver creature");
    }
}
