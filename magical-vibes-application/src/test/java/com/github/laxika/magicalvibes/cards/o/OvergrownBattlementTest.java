package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
