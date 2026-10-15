package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mulch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReclusiveTaxidermist.class, Mulch.class})
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

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void tappingCanProduceEachOtherColor(ManaColor color) {
        Permanent taxidermist = addReadyTaxidermist();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(taxidermist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
    }

    @Test
    void summoningSicknessPreventsManaAbility() {
        Permanent taxidermist = harness.addToBattlefieldAndReturn(player1, new ReclusiveTaxidermist());
        taxidermist.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(taxidermist.isTapped()).isFalse();
    }

    @Test
    void noncreatureCardsDoNotCountTowardThreshold() {
        harness.setGraveyard(player1, List.of(new ReclusiveTaxidermist(),
                new ReclusiveTaxidermist(), new ReclusiveTaxidermist(), new Mulch()));
        Permanent taxidermist = addReadyTaxidermist();

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(2);
    }

    @Test
    void gainsBonusImmediatelyWhenFourthCreatureCardEntersGraveyard() {
        harness.setGraveyard(player1, graveyardWithCreatureCards(3));
        Permanent taxidermist = addReadyTaxidermist();
        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(2);

        harness.setGraveyard(player1, graveyardWithCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(4);

        harness.setGraveyard(player1, graveyardWithCreatureCards(5));

        assertThat(gqs.getEffectivePower(gd, taxidermist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, taxidermist)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyTaxidermist() {
        Permanent taxidermist = harness.addToBattlefieldAndReturn(player1, new ReclusiveTaxidermist());
        taxidermist.setSummoningSick(false);
        return taxidermist;
    }

    private List<Card> graveyardWithCreatureCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new ReclusiveTaxidermist())
                .toList();
    }
}
