package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Werebear.class, WoodlandDruid.class})
class WerebearTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Werebear produces one green mana")
    void tappingProducesGreenMana() {
        Permanent werebear = addCreatureReady(player1, new Werebear());
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
        Permanent werebear = addCreatureReady(player1, new Werebear());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Werebear gets +3/+3 with seven cards in its controller's graveyard")
    void thresholdBonusAtSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent werebear = addCreatureReady(player1, new Werebear());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Werebear keeps its threshold bonus with more than seven cards in its controller's graveyard")
    void thresholdBonusAboveSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(8));
        Permanent werebear = addCreatureReady(player1, new Werebear());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Werebear's threshold bonus affects only Werebear")
    void thresholdBonusAffectsOnlyWerebear() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent werebear = addCreatureReady(player1, new Werebear());
        Permanent woodlandDruid = addCreatureReady(player1, new WoodlandDruid());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, woodlandDruid)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodlandDruid)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable Werebear's threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithCards(7));
        Permanent werebear = addCreatureReady(player1, new Werebear());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Werebear loses its threshold bonus when its controller drops below seven cards")
    void losesThresholdBonusWhenGraveyardDropsBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent werebear = addCreatureReady(player1, new Werebear());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);

        harness.setGraveyard(player1, graveyardWithCards(6));

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Werebear gains threshold immediately when its controller reaches seven cards")
    void gainsThresholdWhenGraveyardReachesSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent werebear = addCreatureReady(player1, new Werebear());

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(1);

        harness.setGraveyard(player1, graveyardWithCards(7));

        assertThat(gqs.getEffectivePower(gd, werebear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werebear)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick Werebear cannot activate its tap mana ability")
    void summoningSickCannotActivateManaAbility() {
        Permanent werebear = harness.addToBattlefieldAndReturn(player1, new Werebear());
        werebear.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(werebear.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Werebear cannot pay its tap cost again while tapped")
    void tappedCannotActivateManaAbilityAgain() {
        Permanent werebear = addCreatureReady(player1, new Werebear());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(werebear.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new WoodlandDruid())
                .toList();
    }
}
