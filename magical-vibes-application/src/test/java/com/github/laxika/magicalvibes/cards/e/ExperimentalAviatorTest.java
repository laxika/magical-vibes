package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExperimentalAviator.class})
class ExperimentalAviatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 colorless Thopter artifact creature tokens with flying")
    void etbCreatesTwoThopters() {
        harness.castFromHand(player1, new ExperimentalAviator(), "{3}{U}{U}");
        resolveAllTriggers();

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
    }

    @Test
    @DisplayName("Entering without being cast creates Thopters for the entering creature's controller")
    void noncastEntryCreatesTokensForController() {
        harness.enterBattlefieldAndReturn(player2, new ExperimentalAviator());

        assertThat(countPermanents(player2, "Thopter")).isZero();
        resolveAllTriggers();

        List<Permanent> thopters = findPermanents(player2, "Thopter");
        assertThat(thopters).hasSize(2);
        assertThat(countPermanents(player1, "Thopter")).isZero();
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(thopter.getCard().isToken()).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
            assertThat(thopter.getCard().getColors()).isEmpty();
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.isTapped()).isFalse();
            assertThat(thopter.isSummoningSick()).isTrue();
        });
    }
}
