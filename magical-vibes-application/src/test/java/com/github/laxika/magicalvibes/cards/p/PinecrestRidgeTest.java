package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PinecrestRidge.class)
class PinecrestRidgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for red adds {R} and the land stays tapped through the next untap step")
    void tapsForRedAndSkipsNextUntap() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(ridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green adds {G} and the land untaps again on the following turn")
    void tapsForGreenAndSkipsOnlyOneUntapStep() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isFalse();
    }

    private Permanent addRidge() {
        Permanent ridge = harness.addToBattlefieldAndReturn(player1, new PinecrestRidge());
        ridge.setSummoningSick(false);
        return ridge;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
