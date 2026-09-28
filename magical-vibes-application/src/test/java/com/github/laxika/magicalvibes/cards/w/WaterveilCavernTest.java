package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WaterveilCavern.class)
class WaterveilCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent cavern = addCavern();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(cavern.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and the land stays tapped through the next untap step")
    void tapsForBlueAndSkipsNextUntap() {
        Permanent cavern = addCavern();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(cavern.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black adds {B} and the land untaps again on the following turn")
    void tapsForBlackAndSkipsOnlyOneUntapStep() {
        Permanent cavern = addCavern();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.BLACK)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(cavern.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(cavern.isTapped()).isFalse();
    }

    private Permanent addCavern() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new WaterveilCavern());
        cavern.setSummoningSick(false);
        return cavern;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
