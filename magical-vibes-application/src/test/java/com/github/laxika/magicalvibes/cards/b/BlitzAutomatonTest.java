package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlitzAutomatonTest extends BaseCardTest {

    @Test
    void normalCastUsesPrintedCharacteristics() {
        harness.setHand(player1, List.of(new BlitzAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Blitz Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, automaton)).isEmpty();
    }

    @Test
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new BlitzAutomaton()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Blitz Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, automaton)).containsExactly(CardColor.RED);
    }
}
