package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RepeatOffender.class)
class RepeatOffenderTest extends BaseCardTest {

    @Test
    @DisplayName("The first activation suspects Repeat Offender")
    void firstActivationSuspectsCreature() {
        Permanent repeatOffender = addReadyRepeatOffender();

        activate();

        assertThat(repeatOffender.isSuspected()).isTrue();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A later activation puts a +1/+1 counter on a suspected Repeat Offender")
    void laterActivationAddsCounter() {
        Permanent repeatOffender = addReadyRepeatOffender();

        activate();
        activate();

        assertThat(repeatOffender.isSuspected()).isTrue();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Stacked activations check suspicion when each ability resolves")
    void stackedActivationsUseCurrentSuspicion() {
        Permanent repeatOffender = addReadyRepeatOffender();
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(repeatOffender.isSuspected()).isFalse();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(repeatOffender.isSuspected()).isTrue();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent repeatOffender = harness.addToBattlefieldAndReturn(player1, new RepeatOffender());
        repeatOffender.setSummoningSick(true);
        repeatOffender.setTapped(true);

        activate();

        assertThat(repeatOffender.isSuspected()).isTrue();
        assertThat(repeatOffender.isTapped()).isTrue();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Becoming suspected grants menace and prevents blocking")
    void suspicionGrantsMenaceAndPreventsBlocking() {
        Permanent repeatOffender = addReadyRepeatOffender();
        Permanent attacker = addCreatureReady(player2, new RepeatOffender());

        assertThat(bls.canBlockAttacker(gd, repeatOffender, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();

        activate();

        assertThat(gqs.hasKeyword(gd, repeatOffender, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlockAttacker(gd, repeatOffender, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    private Permanent addReadyRepeatOffender() {
        return addCreatureReady(player1, new RepeatOffender());
    }

    private void activate() {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
