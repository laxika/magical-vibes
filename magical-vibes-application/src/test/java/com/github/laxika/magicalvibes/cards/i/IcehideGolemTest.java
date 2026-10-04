package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcehideGolem.class})
class IcehideGolemTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(ManaColor.class)
    void castsWithOneManaFromASnowSource(ManaColor color) {
        IcehideGolem golem = new IcehideGolem();
        harness.setHand(player1, List.of(golem));
        gd.playerManaPools.get(player1.getId()).addSnowMana(color, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(golem.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @ParameterizedTest
    @EnumSource(ManaColor.class)
    void cannotCastWithNonsnowMana(ManaColor color) {
        IcehideGolem golem = new IcehideGolem();
        harness.setHand(player1, List.of(golem));
        harness.addMana(player1, color, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(golem);
        assertThat(gd.stack).isEmpty();
    }
}
