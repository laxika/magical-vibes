package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlitzAutomaton.class, MachineOverMatter.class})
class BlitzAutomatonTest extends BaseCardTest {

    @Test
    void normalCastUsesPrintedCharacteristics() {
        harness.castFromHand(player1, new BlitzAutomaton(), "{7}");
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

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Blitz Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, automaton)).containsExactly(CardColor.RED);
    }

    @Test
    void normalCastCanAttackImmediately() {
        harness.castFromHand(player1, new BlitzAutomaton(), "{7}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void prototypeCastCanAttackImmediately() {
        harness.setHand(player1, List.of(new BlitzAutomaton()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void returnedPrototypeCanBeCastNormallyWithNormalCharacteristics() {
        harness.setHand(player1, List.of(new BlitzAutomaton()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent prototype = findPermanent(player1, "Blitz Automaton");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, prototype.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Blitz Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, automaton)).isEmpty();
    }
}
