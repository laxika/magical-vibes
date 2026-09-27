package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CloudcrestLake.class)
class CloudcrestLakeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent lake = addLake();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(lake.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(lake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and the land stays tapped through the next untap step")
    void tapsForWhiteAndSkipsNextUntap() {
        Permanent lake = addLake();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.WHITE)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(lake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and the land untaps again on the following turn")
    void tapsForBlueAndSkipsOnlyOneUntapStep() {
        Permanent lake = addLake();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(lake.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(lake.isTapped()).isFalse();
    }

    private Permanent addLake() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new CloudcrestLake());
        lake.setSummoningSick(false);
        return lake;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
