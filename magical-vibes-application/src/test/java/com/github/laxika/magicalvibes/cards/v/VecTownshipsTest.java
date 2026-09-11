package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VecTownships.class)
class VecTownshipsTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds colorless without locking the untap step")
    void colorlessAbilityDoesNotSkipUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VecTownships());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isEqualTo(0);

        harness.performUntapStep(player1);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Green ability adds {G} and locks the next untap step")
    void greenAbilityAddsGreenAndSkipsUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VecTownships());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("White ability adds {W} and locks the next untap step")
    void whiteAbilityAddsWhiteAndSkipsUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VecTownships());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Colored abilities skip exactly one untap step")
    void coloredAbilitiesSkipExactlyOneUntapStep() {
        Permanent greenLand = harness.addToBattlefieldAndReturn(player1, new VecTownships());
        Permanent whiteLand = harness.addToBattlefieldAndReturn(player1, new VecTownships());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 2, null, null);

        harness.performUntapStep(player1);

        assertThat(greenLand.isTapped()).isTrue();
        assertThat(whiteLand.isTapped()).isTrue();
        assertThat(greenLand.getSkipUntapCount()).isZero();
        assertThat(whiteLand.getSkipUntapCount()).isZero();

        harness.performUntapStep(player1);

        assertThat(greenLand.isTapped()).isFalse();
        assertThat(whiteLand.isTapped()).isFalse();
    }
}
