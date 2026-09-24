package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BagEndBanquet.class)
class BagEndBanquetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three Food tokens")
    void entersWithThreeFoodTokens() {
        castBanquet();

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds one colorless mana for each Food controlled")
    void addsManaForEachFoodControlled() {
        castBanquet();
        Permanent banquet = findPermanent(player1, "Bag End Banquet");
        int banquetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(banquet);

        harness.activateAbility(player1, banquetIndex, null, null);

        assertThat(banquet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private void castBanquet() {
        harness.setHand(player1, List.of(new BagEndBanquet()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
