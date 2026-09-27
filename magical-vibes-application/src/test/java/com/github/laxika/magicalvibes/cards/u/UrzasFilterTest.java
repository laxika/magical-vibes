package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BlurredMongoose;
import com.github.laxika.magicalvibes.cards.g.GalinasKnight;
import com.github.laxika.magicalvibes.cards.h.HornedCheetah;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasFilter.class, HornedCheetah.class, BlurredMongoose.class, GalinasKnight.class})
class UrzasFilterTest extends BaseCardTest {

    @Test
    @DisplayName("Multicolored spells cost {2} less")
    void multicoloredSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new UrzasFilter());
        // Horned Cheetah {2}{G}{W} reduced to {G}{W}.
        harness.castFromHand(player1, new HornedCheetah(), "{G}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Horned Cheetah");
    }

    @Test
    @DisplayName("Monocolored spells are not reduced")
    void monocoloredSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new UrzasFilter());

        // Blurred Mongoose {1}{G} is unaffected; one green is not enough.
        assertThatThrownBy(() -> harness.castFromHand(player1, new BlurredMongoose(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction also applies to an opponent's multicolored spells")
    void opponentMulticoloredSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new UrzasFilter());
        harness.forceActivePlayer(player2);
        // Horned Cheetah {2}{G}{W} reduced to {G}{W} for the opponent too.
        harness.castFromHand(player2, new HornedCheetah(), "{G}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Horned Cheetah");
    }

    @Test
    @DisplayName("The reduction does not pay colored mana requirements")
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new UrzasFilter());

        // Galina's Knight {W}{U} has no generic mana to reduce; one white is not enough.
        assertThatThrownBy(() -> harness.castFromHand(player1, new GalinasKnight(), "{W}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
