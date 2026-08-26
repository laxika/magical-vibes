package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticFamiliar.class, GrizzlyBears.class, Terror.class})
class MysticFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 1/2 without threshold and has no protection from black")
    void noThresholdBonusBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent familiar = addFamiliar(player1);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, familiar, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and protection from black at threshold")
    void thresholdBonusAtSevenCards() {
        fillGraveyard(player1, 7);
        Permanent familiar = addFamiliar(player1);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, familiar, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Only its controller's graveyard counts")
    void opponentGraveyardDoesNotCount() {
        fillGraveyard(player2, 7);
        Permanent familiar = addFamiliar(player1);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, familiar, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Loses threshold abilities when its controller has fewer than seven graveyard cards")
    void losesThresholdAbilitiesBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent familiar = addFamiliar(player1);
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, familiar, CardColor.BLACK)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, familiar, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell while threshold is active")
    void cannotBeTargetedByBlackSpellAtThreshold() {
        fillGraveyard(player1, 7);
        Permanent familiar = addFamiliar(player1);

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, familiar.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private Permanent addFamiliar(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MysticFamiliar());
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setGraveyard(player, cards);
    }
}
