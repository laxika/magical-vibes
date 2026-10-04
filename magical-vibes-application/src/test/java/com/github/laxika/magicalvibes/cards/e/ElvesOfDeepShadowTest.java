package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ElvesOfDeepShadow.class)
class ElvesOfDeepShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds {B} and deals 1 damage to its controller")
    void tapForBlackMana() {
        Permanent elves = addCreatureReady(player1, new ElvesOfDeepShadow());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new ElvesOfDeepShadow());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ElvesOfDeepShadow());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Mana ability damage is attributed to the Elves permanent")
    void damageIsDealtByElves() {
        Permanent elves = addCreatureReady(player1, new ElvesOfDeepShadow());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.damageDealtThisTurnBySource.get(elves.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate at one life because the damage is not a life payment")
    void canActivateAtOneLife() {
        addCreatureReady(player1, new ElvesOfDeepShadow());
        harness.setLife(player1, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, 0);
        harness.assertLife(player2, 20);
    }
}
