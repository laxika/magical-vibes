package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZofConsumption.class, ZofBloodbog.class})
class ZofConsumptionTest extends BaseCardTest {

    @Test
    void consumptionMakesEachOpponentLoseFourAndControllerGainFour() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ZofConsumption()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void bloodbogEntersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new ZofConsumption()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(ZofBloodbog.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }
}
