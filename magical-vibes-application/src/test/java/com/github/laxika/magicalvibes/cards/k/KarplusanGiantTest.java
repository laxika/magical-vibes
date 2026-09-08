package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarplusanGiant.class, Mountain.class, SnowCoveredMountain.class})
@DisplayName("Karplusan Giant")
class KarplusanGiantTest extends BaseCardTest {

    private Permanent addSnowMountain() {
        return harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());
    }

    @Test
    @DisplayName("Tapping a snow land gives +1/+1")
    void tappingSnowLandBoosts() {
        Permanent giant = addCreatureReady(player1, new KarplusanGiant());
        Permanent snow = addSnowMountain();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(snow.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate while summoning sick because the source is not tapped")
    void canActivateWhileSummoningSick() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new KarplusanGiant());
        giant.setSummoningSick(true);
        Permanent snow = addSnowMountain();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(snow.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each activation taps a separate snow land")
    void eachActivationTapsOneLand() {
        addCreatureReady(player1, new KarplusanGiant());
        Permanent first = addSnowMountain();
        Permanent second = addSnowMountain();

        Permanent giant = findPermanent(player1, "Karplusan Giant");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();

        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent giant = addCreatureReady(player1, new KarplusanGiant());
        addSnowMountain();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate with only a nonsnow land")
    void cannotActivateWithoutSnowLand() {
        addCreatureReady(player1, new KarplusanGiant());
        harness.addToBattlefield(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when the only snow land is already tapped")
    void cannotActivateWithTappedSnowLand() {
        addCreatureReady(player1, new KarplusanGiant());
        addSnowMountain().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot tap an opponent's snow land")
    void cannotActivateUsingOpponentsSnowLand() {
        addCreatureReady(player1, new KarplusanGiant());
        harness.addToBattlefieldAndReturn(player2, new SnowCoveredMountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
