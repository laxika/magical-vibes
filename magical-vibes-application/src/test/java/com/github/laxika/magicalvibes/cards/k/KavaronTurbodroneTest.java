package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KavaronTurbodrone.class, GrizzlyBears.class})
class KavaronTurbodroneTest extends BaseCardTest {

    @Test
    @DisplayName("Kavaron Turbodrone boosts a creature you control and gives it haste")
    void boostsAndGrantsHaste() {
        Permanent turbodrone = addCreatureReady(player1, new KavaronTurbodrone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareForActivation();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(turbodrone.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Kavaron Turbodrone's boost and haste last until end of turn")
    void effectExpiresAtEndOfTurn() {
        addCreatureReady(player1, new KavaronTurbodrone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareForActivation();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Kavaron Turbodrone can target only a creature you control")
    void targetMustBeOwnCreature() {
        addCreatureReady(player1, new KavaronTurbodrone());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kavaron Turbodrone's ability can be activated only at sorcery speed")
    void sorcerySpeedOnly() {
        addCreatureReady(player1, new KavaronTurbodrone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
