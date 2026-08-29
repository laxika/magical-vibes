package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KitsuneLoreweaverTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives toughness equal to the current hand size")
    void abilityUsesCurrentHandSize() {
        Permanent loreweaver = addLoreweaverReady(player1);
        harness.setHand(player1, hand(3));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(loreweaver.getEffectivePower()).isEqualTo(2);
        assertThat(loreweaver.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability reads hand size when it resolves and wears off at end of turn")
    void abilityReadsHandSizeAtResolutionAndExpires() {
        Permanent loreweaver = addLoreweaverReady(player1);
        harness.setHand(player1, hand(1));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, hand(4));
        harness.passBothPriorities();

        assertThat(loreweaver.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(loreweaver.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addLoreweaverReady(Player player) {
        return addCreatureReady(player, new KitsuneLoreweaver());
    }

    private List<Card> hand(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> new GrizzlyBears())
                .map(card -> (Card) card)
                .toList();
    }
}
