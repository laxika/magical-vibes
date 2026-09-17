package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TimelessLotus.class)
class TimelessLotusTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new TimelessLotus()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void addsOneManaOfEachColor() {
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new TimelessLotus());
        lotus.setSummoningSick(false);
        lotus.untap();

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(lotus.isTapped()).isTrue();
    }
}
