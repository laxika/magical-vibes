package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LanternLitGraveyard.class)
class LanternLitGraveyardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent land = addLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for black adds {B} and the land stays tapped through the next untap step")
    void tapsForBlackAndSkipsNextUntap() {
        Permanent land = addLand();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.BLACK)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red adds {R} and the land untaps again on the following turn")
    void tapsForRedAndSkipsOnlyOneUntapStep() {
        Permanent land = addLand();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(land.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(land.isTapped()).isFalse();
    }

    private Permanent addLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new LanternLitGraveyard());
        land.setSummoningSick(false);
        return land;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
