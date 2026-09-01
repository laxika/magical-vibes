package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanizingSawship.class, GrizzlyBears.class})
class GalvanizingSawshipTest extends BaseCardTest {

    @Test
    void stationUsesTappedCreaturePower() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(sawship), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(sawship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void threeChargeCountersMakeTheSawshipAnArtifactCreatureWithFlyingAndHaste() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());

        assertThat(gqs.isCreature(gd, sawship)).isFalse();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.HASTE)).isFalse();

        sawship.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.isCreature(gd, sawship)).isTrue();
        assertThat(gqs.isArtifact(gd, sawship)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sawship)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sawship)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.HASTE)).isTrue();
    }

    @Test
    void stationRequiresAnotherUntappedCreature() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sawship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
