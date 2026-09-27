package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SapphireLeech.class, DreamThrush.class, AlabasterLeech.class})
class SapphireLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("Blue spells you cast cost {U} more")
    class OwnBlueSpellsTaxed {

        @Test
        @DisplayName("A blue creature cannot be cast without the additional mana")
        void blueSpellCostsMore() {
            harness.addToBattlefield(player1, new SapphireLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new DreamThrush(), "{U}{U}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A blue creature is castable with one extra mana")
        void blueSpellCastableWithTax() {
            harness.addToBattlefield(player1, new SapphireLeech());
            harness.castFromHand(player1, new DreamThrush(), "{U}{U}{U}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("The additional blue mana cannot be paid with colorless mana")
        void blueSpellNeedsBlueTax() {
            harness.addToBattlefield(player1, new SapphireLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new DreamThrush(), "{2}{U}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Only the controller's blue spells are taxed")
    class OpponentAndNonBlueNotTaxed {

        @Test
        @DisplayName("A non-blue spell cast by the controller is not taxed")
        void nonBlueSpellNotAffected() {
            harness.addToBattlefield(player1, new SapphireLeech());
            harness.castFromHand(player1, new AlabasterLeech(), "{W}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's blue spell is not taxed")
        void opponentBlueSpellNotAffected() {
            harness.addToBattlefield(player1, new SapphireLeech());

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new DreamThrush(), "{U}{U}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
