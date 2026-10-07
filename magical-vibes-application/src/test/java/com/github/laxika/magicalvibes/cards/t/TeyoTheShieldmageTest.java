package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.model.CardColor;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeyoTheShieldmage.class, ChandrasPyrohelix.class})
class TeyoTheShieldmageTest extends BaseCardTest {

    @Test
    @DisplayName("Protects its controller with hexproof")
    void protectsControllerWithHexproof() {
        addReadyTeyo(player1, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, Map.of(player1.getId(), 2)))
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

    @Test
    void controllerCanTargetThemself() {
        addReadyTeyo(player1, 3);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotProtectTeyoItself() {
        Permanent teyo = addReadyTeyo(player1, 3);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, Map.of(teyo.getId(), 2));
        harness.passBothPriorities();

        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void protectionEndsWhenLastLoyaltyIsSpentButTokenStillResolves() {
        addReadyTeyo(player1, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Teyo, the Shieldmage");
        harness.assertInGraveyard(player1, "Teyo, the Shieldmage");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.getCard().isToken()).isTrue();
        assertThat(wall.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(wall.getEffectivePower()).isZero();
        assertThat(wall.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();

        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, Map.of(player1.getId(), 2));
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotActivateWithoutTwoLoyaltyCounters() {
        Permanent teyo = addReadyTeyo(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(teyo);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyTeyo(Player player, int loyalty) {
        Permanent teyo = harness.addToBattlefieldAndReturn(player, new TeyoTheShieldmage());
        teyo.setCounterCount(CounterType.LOYALTY, loyalty);
        teyo.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return teyo;
    }
}
