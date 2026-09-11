package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BleedingWoods.class)
class BleedingWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when every player has more than 13 life")
    void entersTappedWhenEveryPlayerHasMoreThanThirteenLife() {
        playWoods(14, 14);

        assertThat(findWoods(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has 13 or less life")
    void entersUntappedWhenControllerHasThirteenOrLessLife() {
        playWoods(13, 20);

        assertThat(findWoods(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when an opponent has 13 or less life")
    void entersUntappedWhenOpponentHasThirteenOrLessLife() {
        playWoods(20, 13);

        assertThat(findWoods(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Produces red mana")
    void producesRedMana() {
        Permanent woods = addWoodsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(woods.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Produces green mana")
    void producesGreenMana() {
        Permanent woods = addWoodsReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(woods.isTapped()).isTrue();
    }

    private void playWoods(int controllerLife, int opponentLife) {
        harness.setLife(player1, controllerLife);
        harness.setLife(player2, opponentLife);
        harness.setHand(player1, List.of(new BleedingWoods()));
        harness.playLand(player1, 0);
    }

    private Permanent addWoodsReady(Player player) {
        Permanent woods = new Permanent(new BleedingWoods());
        woods.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(woods);
        return woods;
    }

    private Permanent findWoods(Player player) {
        return findPermanent(player, "Bleeding Woods");
    }
}
