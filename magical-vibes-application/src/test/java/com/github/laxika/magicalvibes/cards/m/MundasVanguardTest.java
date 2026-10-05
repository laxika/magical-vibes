package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AffaProtector;
import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MundasVanguard.class, AffaProtector.class, CanopyGorger.class})
class MundasVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and puts a +1/+1 counter on each creature you control")
    void cohortPutsCountersOnEachCreatureYouControl() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());
        Permanent ally = addCreatureReady(player1, new AffaProtector());
        Permanent nonAlly = addCreatureReady(player1, new CanopyGorger());
        Permanent opponentCreature = addCreatureReady(player2, new CanopyGorger());

        harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null);
        harness.passBothPriorities();

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("A summoning-sick Ally can pay the additional tap cost")
    void summoningSickAllyCanPayCohortCost() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AffaProtector());
        ally.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null);

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Vanguard cannot activate cohort")
    void summoningSickVanguardCannotActivate() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new MundasVanguard());
        vanguard.setSummoningSick(true);
        Permanent ally = addCreatureReady(player1, new AffaProtector());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(ally.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped Allies, non-Allies, and opposing Allies cannot pay the cohort cost")
    void ineligibleCreaturesCannotPayCohortCost() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());
        Permanent tappedAlly = addCreatureReady(player1, new AffaProtector());
        tappedAlly.tap();
        Permanent nonAlly = addCreatureReady(player1, new CanopyGorger());
        Permanent opposingAlly = addCreatureReady(player2, new AffaProtector());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(nonAlly.isTapped()).isFalse();
        assertThat(opposingAlly.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort affects creatures present at resolution even if Vanguard has left")
    void usesCreaturesAtResolutionAndSurvivesSourceLeaving() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());
        Permanent ally = addCreatureReady(player1, new AffaProtector());

        harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(vanguard);
        gd.playerGraveyards.get(player1.getId()).add(vanguard.getCard());
        Permanent newCreature = harness.enterBattlefieldAndReturn(player1, new CanopyGorger());

        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
