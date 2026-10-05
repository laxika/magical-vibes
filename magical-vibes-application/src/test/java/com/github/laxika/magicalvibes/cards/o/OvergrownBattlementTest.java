package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvergrownBattlement.class, GrizzlyBears.class, WallOfStone.class})
class OvergrownBattlementTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Overgrown Battlement adds one green mana for each controlled creature with defender")
    void addsGreenManaForEachControlledDefender() {
        addCreatureReady(player1, new OvergrownBattlement());
        addCreatureReady(player1, new WallOfStone());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WallOfStone());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping Overgrown Battlement counts itself when it is the only controlled defender")
    void countsItselfAsDefender() {
        addCreatureReady(player1, new OvergrownBattlement());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped and summoning-sick defenders still count toward mana production")
    void countsTappedAndSummoningSickDefenders() {
        var source = addCreatureReady(player1, new OvergrownBattlement());
        addCreatureReady(player1, new OvergrownBattlement()).setTapped(true);
        addCreatureReady(player1, new OvergrownBattlement()).setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Battlement cannot activate again")
    void cannotActivateTwiceWithoutUntapping() {
        addCreatureReady(player1, new OvergrownBattlement());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Battlement cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        var source = addCreatureReady(player1, new OvergrownBattlement());
        source.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
