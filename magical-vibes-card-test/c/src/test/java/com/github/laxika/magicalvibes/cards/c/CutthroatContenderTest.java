package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CutthroatContender.class})
class CutthroatContenderTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 1 life gives Cutthroat Contender +1/+0 until end of turn")
    void payLifeBoostsSelf() {
        Permanent contender = addReadyContender(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, contender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, contender)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void secondActivationSameTurnRejected() {
        addReadyContender(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent contender = addReadyContender(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, contender)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, contender)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addReadyContender(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Life is paid and the activation limit applies before the ability resolves")
    void costAndLimitApplyWhileAbilityIsOnStack() {
        Permanent contender = addReadyContender(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, contender)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, contender)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick contender can activate its ability")
    void tappedSummoningSickContenderCanActivate() {
        Permanent contender = harness.addToBattlefieldAndReturn(player1, new CutthroatContender());
        contender.setSummoningSick(true);
        contender.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, contender)).isEqualTo(2);
        assertThat(contender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each contender has its own activation limit and boosts only itself")
    void activationLimitIsPerPermanent() {
        Permanent first = addReadyContender(player1);
        Permanent second = addReadyContender(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    private Permanent addReadyContender(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CutthroatContender());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
