package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SatyrHedonist.class})
class SatyrHedonistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself to add three red mana")
    void sacrificesItselfForThreeRedMana() {
        harness.addToBattlefield(player1, new SatyrHedonist());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Satyr Hedonist");
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void requiresRedMana() {
        harness.addToBattlefield(player1, new SatyrHedonist());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick without using the stack")
    void activatesWhileTappedAndSummoningSick() {
        var hedonist = harness.addToBattlefieldAndReturn(player1, new SatyrHedonist());
        hedonist.tap();
        hedonist.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Satyr Hedonist");
    }

    @Test
    @DisplayName("Green mana cannot pay the red cost and failed activation does not sacrifice the creature")
    void greenManaCannotPayActivationCost() {
        harness.addToBattlefield(player1, new SatyrHedonist());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Satyr Hedonist");
        harness.assertNotInGraveyard(player1, "Satyr Hedonist");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

}
