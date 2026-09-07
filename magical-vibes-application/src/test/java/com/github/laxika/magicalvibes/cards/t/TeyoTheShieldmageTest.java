package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeyoTheShieldmage.class, Shock.class})
class TeyoTheShieldmageTest extends BaseCardTest {

    @Test
    @DisplayName("Protects its controller with hexproof")
    void protectsControllerWithHexproof() {
        addReadyTeyo(player1, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-2 creates a 0/3 white Wall token with defender")
    void minusTwoCreatesWallToken() {
        Permanent teyo = addReadyTeyo(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(wall.getEffectivePower()).isZero();
        assertThat(wall.getEffectiveToughness()).isEqualTo(3);
        assertThat(wall.getCard().getSubtypes()).contains(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyTeyo(Player player, int loyalty) {
        Permanent teyo = new Permanent(new TeyoTheShieldmage());
        teyo.setCounterCount(CounterType.LOYALTY, loyalty);
        teyo.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(teyo);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return teyo;
    }
}
