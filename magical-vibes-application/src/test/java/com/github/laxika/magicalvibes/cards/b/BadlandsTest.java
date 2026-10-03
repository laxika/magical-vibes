package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Badlands.class)
class BadlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Badlands produces black mana")
    void producesBlackMana() {
        Permanent badlands = addBadlandsReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(badlands.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Badlands produces red mana")
    void producesRedMana() {
        Permanent badlands = addBadlandsReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(badlands.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Badlands can produce mana immediately after entering and does not use the stack")
    void producesManaImmediatelyAfterEntering() {
        Permanent badlands = harness.enterBattlefieldAndReturn(player1, new Badlands());

        assertThat(badlands.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(badlands.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Badlands cannot produce both colors from a single tap")
    void cannotActivateAgainWhileTapped() {
        Permanent badlands = harness.addToBattlefieldAndReturn(player1, new Badlands());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent is already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(badlands.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBadlandsReady() {
        Permanent badlands = harness.addToBattlefieldAndReturn(player1, new Badlands());
        badlands.setSummoningSick(false);
        return badlands;
    }
}
