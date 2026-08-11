package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WerebearTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Werebear produces one green mana")
    void tappingProducesGreenMana() {
        Permanent werebear = addReadyWerebear();
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        assertThat(werebear.isTapped()).isTrue();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Werebear has no threshold bonus below seven cards in its controller's graveyard")
    void noThresholdBonusBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent werebear = addReadyWerebear();

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Werebear gets +3/+3 with seven cards in its controller's graveyard")
    void thresholdBonusAtSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent werebear = addReadyWerebear();

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable Werebear's threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithCards(7));
        Permanent werebear = addReadyWerebear();

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Werebear loses its threshold bonus when its controller drops below seven cards")
    void losesThresholdBonusWhenGraveyardDropsBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent werebear = addReadyWerebear();

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);

        harness.setGraveyard(player1, graveyardWithCards(6));

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);
    }

    private Permanent addReadyWerebear() {
        Permanent werebear = harness.addToBattlefieldAndReturn(player1, new Werebear());
        werebear.setSummoningSick(false);
        return werebear;
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
