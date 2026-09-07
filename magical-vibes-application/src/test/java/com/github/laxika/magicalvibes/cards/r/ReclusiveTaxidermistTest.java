package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ReclusiveTaxidermist.class, GrizzlyBears.class})
class ReclusiveTaxidermistTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Reclusive Taxidermist produces the chosen color of mana")
    void tappingProducesChosenColorMana() {
        Permanent taxidermist = addReadyTaxidermist();
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(taxidermist.isTapped()).isTrue();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reclusive Taxidermist is 1/2 with fewer than four creature cards in its controller's graveyard")
    void noGraveyardBonusBelowFourCreatureCards() {
        harness.setGraveyard(player1, graveyardWithCreatureCards(3));
        Permanent taxidermist = addReadyTaxidermist();

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reclusive Taxidermist gets +3/+2 with four creature cards in its controller's graveyard")
    void graveyardBonusAtFourCreatureCards() {
        harness.setGraveyard(player1, graveyardWithCreatureCards(4));
        Permanent taxidermist = addReadyTaxidermist();

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's creature cards do not enable Reclusive Taxidermist's bonus")
    void opponentGraveyardDoesNotEnableBonus() {
        harness.setGraveyard(player2, graveyardWithCreatureCards(4));
        Permanent taxidermist = addReadyTaxidermist();

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reclusive Taxidermist loses its bonus when its controller drops below four creature cards")
    void losesGraveyardBonusWhenCreatureCardsDropBelowThreshold() {
        harness.setGraveyard(player1, graveyardWithCreatureCards(4));
        Permanent taxidermist = addReadyTaxidermist();

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(4);

        harness.setGraveyard(player1, graveyardWithCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(2);
    }

    private Permanent addReadyTaxidermist() {
        Permanent taxidermist = harness.addToBattlefieldAndReturn(player1, new ReclusiveTaxidermist());
        taxidermist.setSummoningSick(false);
        return taxidermist;
    }

    private List<Card> graveyardWithCreatureCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
