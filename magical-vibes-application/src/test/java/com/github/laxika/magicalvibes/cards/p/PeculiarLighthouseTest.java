package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PeculiarLighthouse.class)
class PeculiarLighthouseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when every player has more than 13 life")
    void entersTappedWhenEveryPlayerHasMoreThanThirteenLife() {
        playLighthouse(14, 14);

        assertThat(findLighthouse(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has 13 or less life")
    void entersUntappedWhenControllerHasThirteenOrLessLife() {
        playLighthouse(13, 20);

        assertThat(findLighthouse(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when an opponent has 13 or less life")
    void entersUntappedWhenOpponentHasThirteenOrLessLife() {
        playLighthouse(20, 13);

        assertThat(findLighthouse(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Produces blue mana")
    void producesBlueMana() {
        tapFor(ManaColor.BLUE, 0);
    }

    @Test
    @DisplayName("Produces red mana")
    void producesRedMana() {
        tapFor(ManaColor.RED, 1);
    }

    private void playLighthouse(int controllerLife, int opponentLife) {
        harness.setLife(player1, controllerLife);
        harness.setLife(player2, opponentLife);
        harness.setHand(player1, List.of(new PeculiarLighthouse()));
        harness.playLand(player1, 0);
    }

    private void tapFor(ManaColor color, int abilityIndex) {
        Permanent lighthouse = addLighthouseReady(player1);

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(lighthouse.isTapped()).isTrue();
    }

    private Permanent addLighthouseReady(Player player) {
        Permanent lighthouse = new Permanent(new PeculiarLighthouse());
        lighthouse.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lighthouse);
        return lighthouse;
    }

    private Permanent findLighthouse(Player player) {
        return findPermanent(player, "Peculiar Lighthouse");
    }
}
