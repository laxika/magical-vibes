package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

        assertThat(findPermanent(player1, "Peculiar Lighthouse").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has 13 or less life")
    void entersUntappedWhenControllerHasThirteenOrLessLife() {
        playLighthouse(13, 20);

        assertThat(findPermanent(player1, "Peculiar Lighthouse").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when an opponent has 13 or less life")
    void entersUntappedWhenOpponentHasThirteenOrLessLife() {
        playLighthouse(20, 13);

        assertThat(findPermanent(player1, "Peculiar Lighthouse").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when its controller is below 13 life")
    void entersUntappedWhenControllerIsBelowThirteenLife() {
        playLighthouse(12, 20);

        assertThat(findPermanent(player1, "Peculiar Lighthouse").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when its opponent is below 13 life")
    void entersUntappedWhenOpponentIsBelowThirteenLife() {
        playLighthouse(20, 12);

        assertThat(findPermanent(player1, "Peculiar Lighthouse").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering without being played still checks life totals")
    void enteringWithoutBeingPlayedChecksLifeTotals() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);

        Permanent tapped = harness.enterBattlefieldAndReturn(player1, new PeculiarLighthouse());
        assertThat(tapped.isTapped()).isTrue();

        harness.setLife(player2, 13);
        Permanent untapped = harness.enterBattlefieldAndReturn(player1, new PeculiarLighthouse());
        assertThat(untapped.isTapped()).isFalse();
        assertThat(tapped.isTapped()).isTrue();

        harness.setLife(player2, 14);
        assertThat(untapped.isTapped()).isFalse();
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
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new PeculiarLighthouse());

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(lighthouse.isTapped()).isTrue();
    }

}
