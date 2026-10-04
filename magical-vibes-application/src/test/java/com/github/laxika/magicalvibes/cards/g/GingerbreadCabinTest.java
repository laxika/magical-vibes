package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GingerbreadCabin.class, Forest.class})
class GingerbreadCabinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and creates no Food with fewer than three other Forests")
    void entersTappedWithFewerThanThreeForests() {
        addForests(player1, 2);

        playCabin();

        assertThat(findCabin(player1).isTapped()).isTrue();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Enters untapped and creates a Food with three other Forests")
    void entersUntappedWithThreeForestsAndCreatesFood() {
        addForests(player1, 3);

        playCabin();
        harness.passBothPriorities();

        assertThat(findCabin(player1).isTapped()).isFalse();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates Food even if it is tapped before the trigger resolves")
    void createsFoodEvenIfTappedBeforeTriggerResolves() {
        addForests(player1, 3);

        playCabin();
        Permanent cabin = findCabin(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cabin), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts Forests controlled by the cabin's controller")
    void opponentForestsDoNotCount() {
        addForests(player2, 3);

        playCabin();

        assertThat(findCabin(player1).isTapped()).isTrue();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Another Gingerbread Cabin counts as an other Forest even when tapped")
    void anotherCabinCountsAsForest() {
        addForests(player1, 2);
        Permanent otherCabin = harness.addToBattlefieldAndReturn(player1, new GingerbreadCabin());
        otherCabin.tap();

        playCabin();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gingerbread Cabin").get(1).isTapped()).isFalse();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and gains three life on resolution")
    void foodCanBeSacrificedForLife() {
        addForests(player1, 3);
        playCabin();
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent food = findPermanent(player1, "Food");

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), 0, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    private void playCabin() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GingerbreadCabin()));
        harness.playLand(player1, 0);
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private Permanent findCabin(Player player) {
        return findPermanent(player, "Gingerbread Cabin");
    }
}
