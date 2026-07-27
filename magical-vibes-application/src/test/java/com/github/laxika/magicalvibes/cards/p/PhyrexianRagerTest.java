package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianRagerTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Phyrexian Rager puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Rager");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Phyrexian Rager");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Rager");
    }

    @Test
    @DisplayName("ETB draws a card and loses 1 life")
    void etbDrawsAndLosesLife() {
        harness.setHand(player1, List.of(new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
