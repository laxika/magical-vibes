package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrainPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target player's lands and controller adds the mana they produce")
    void drainsLandMana() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());

        cast();

        assertThat(battlefield).allMatch(Permanent::isTapped);
        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Mana the target player already had is also drained to the controller")
    void drainsPreexistingMana() {
        harness.addMana(player2, ManaColor.RED, 3);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Already-tapped lands and non-lands produce nothing")
    void ignoresTappedAndNonLands() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        forest.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getLast();

        cast();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private void cast() {
        harness.setHand(player1, List.of(new DrainPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
