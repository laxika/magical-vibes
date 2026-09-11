package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloweringRogon.class, EnormousBaloth.class, GrizzlyBears.class})
class GloweringRogonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Beast card in your hand")
    void entersWithCounterForEachBeastCard() {
        GloweringRogon card = new GloweringRogon();
        harness.setHand(player1, List.of(
                card, new EnormousBaloth(), new EnormousBaloth(), new GrizzlyBears()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(card).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Beast cards in its controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        GloweringRogon card = new GloweringRogon();
        harness.setHand(player1, List.of(card, new GrizzlyBears()));
        harness.setHand(player2, List.of(new EnormousBaloth(), new EnormousBaloth()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(card).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Permanent findPermanentForCard(GloweringRogon card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
