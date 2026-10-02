package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AvatarOfWill.class)
class AvatarOfWillTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Avatar of Will for {U}{U} when every opponent has cards in hand")
    void cannotCastWithCardsInOpponentsHands() {
        harness.setHand(player1, List.of(new AvatarOfWill()));
        harness.setHand(player2, List.of(new AvatarOfWill()));

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfWill(), "{U}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Avatar of Will for {U}{U} when an opponent has no cards in hand")
    void canCastWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new AvatarOfWill(), "{U}{U}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can pay the full cost when the opponent has cards in hand")
    void canCastForFullCost() {
        harness.setHand(player2, List.of(new AvatarOfWill()));
        harness.castFromHand(player1, new AvatarOfWill(), "{6}{U}{U}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avatar of Will");
    }

    @Test
    @DisplayName("The reduction does not remove either blue mana requirement")
    void cannotReplaceBlueManaWithGenericMana() {
        harness.setHand(player2, List.of());

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfWill(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent gaining a card after casting does not affect resolution")
    void reductionConditionIsOnlyCheckedWhenCasting() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new AvatarOfWill(), "{U}{U}");
        harness.setHand(player2, List.of(new AvatarOfWill()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Will");
        assertThat(gd.stack).isEmpty();
    }
}
