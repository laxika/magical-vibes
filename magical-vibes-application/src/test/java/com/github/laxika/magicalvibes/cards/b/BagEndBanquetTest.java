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
        resolveAllTriggers();
    }

    @Test
    void foodCanBeSacrificedForLifeAndNoLongerCountsForMana() {
        castBanquet();
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);

        Permanent banquet = findPermanent(player1, "Bag End Banquet");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(banquet), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void tappedFoodStillCountsForMana() {
        castBanquet();
        findPermanents(player1, "Food").forEach(food -> food.tap());
        Permanent banquet = findPermanent(player1, "Bag End Banquet");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(banquet), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void producesNoManaWithoutFood() {
        Permanent banquet = harness.addToBattlefieldAndReturn(player1, new BagEndBanquet());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(banquet), null, null);

        assertThat(banquet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void doesNotCountOpponentsFood() {
        castBanquet();
        Permanent opposingBanquet = harness.addToBattlefieldAndReturn(player2, new BagEndBanquet());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opposingBanquet), null, null);

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
        assertThat(opposingBanquet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
