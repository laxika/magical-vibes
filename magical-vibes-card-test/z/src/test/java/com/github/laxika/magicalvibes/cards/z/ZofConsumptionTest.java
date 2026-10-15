package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZofConsumption.class, ZofBloodbog.class})
class ZofConsumptionTest extends BaseCardTest {

    @Test
    void consumptionMakesEachOpponentLoseFourAndControllerGainFour() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ZofConsumption()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

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
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void consumptionChangesLifeOnlyWhenItResolvesAndGoesToGraveyard() {
        harness.setHand(player1, List.of(new ZofConsumption()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Zof Consumption");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bloodbogUsesALandPlayAndCannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new ZofConsumption(), new ZofConsumption()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
