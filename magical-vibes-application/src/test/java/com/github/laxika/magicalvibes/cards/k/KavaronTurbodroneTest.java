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
        harness.passUntil(player2, TurnStep.UPKEEP);

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

    @Test
    @DisplayName("Kavaron Turbodrone can target itself")
    void canTargetItself() {
        Permanent turbodrone = addCreatureReady(player1, new KavaronTurbodrone());
        prepareForActivation();

        harness.activateAbility(player1, 0, 0, null, turbodrone.getId());
        harness.passBothPriorities();

        assertThat(turbodrone.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, turbodrone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, turbodrone)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, turbodrone, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Turbodrone cannot activate to give itself haste")
    void cannotBypassSummoningSickness() {
        Permanent turbodrone = harness.addToBattlefieldAndReturn(player1, new KavaronTurbodrone());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, turbodrone.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(turbodrone.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kavaron Turbodrone cannot activate during its controller's combat")
    void cannotActivateDuringCombat() {
        Permanent turbodrone = addCreatureReady(player1, new KavaronTurbodrone());
        prepareForActivation();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, turbodrone.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(turbodrone.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Kavaron Turbodrone cannot activate with an ability on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent first = addCreatureReady(player1, new KavaronTurbodrone());
        Permanent second = addCreatureReady(player1, new KavaronTurbodrone());
        prepareForActivation();
        harness.activateAbility(player1, 0, 0, null, first.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(second.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new KavaronTurbodrone());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KavaronTurbodrone());
        prepareForActivation();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability does not affect a target that changes controllers before resolution")
    void changedControllerMakesTargetIllegal() {
        addCreatureReady(player1, new KavaronTurbodrone());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KavaronTurbodrone());
        prepareForActivation();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
