package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritSummoning.class})
class SpiritSummoningTest extends BaseCardTest {

    @Test
    void createsAThreeTwoRedAndWhiteSpiritToken() {
        harness.setHand(player1, List.of(new SpiritSummoning()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getEffectivePower()).isEqualTo(3);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(spirit.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
    }

    @ParameterizedTest
    @CsvSource({"0, 2", "1, 1"})
    void whiteAndMixedHybridPaymentsCreateOneSpiritOnResolution(int redMana, int whiteMana) {
        harness.setHand(player1, List.of(new SpiritSummoning()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.addMana(player1, ManaColor.WHITE, whiteMana);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(spirit -> {
            assertThat(spirit.getCard().isToken()).isTrue();
            assertThat(spirit.isTapped()).isFalse();
            assertThat(spirit.getEffectivePower()).isEqualTo(3);
            assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
            assertThat(spirit.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Spirit Summoning");
    }
}
