package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Karplusan Giant")
class KarplusanGiantTest extends BaseCardTest {

    private Permanent addSnowMountain() {
        Permanent mountain = new Permanent(new Mountain());
        TestCards.mutableCard(mountain).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player1.getId()).add(mountain);
        return mountain;
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate with only a nonsnow land")
    void cannotActivateWithoutSnowLand() {
        addCreatureReady(player1, new KarplusanGiant());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Mountain()));

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
}
