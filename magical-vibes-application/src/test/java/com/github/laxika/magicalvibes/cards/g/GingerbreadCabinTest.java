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
