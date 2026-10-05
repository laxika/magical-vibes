package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrysaTideChoreographer.class, AvatarOfMight.class, GrizzlyBears.class, Forest.class})
class OrysaTideChoreographerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent creatures do not contribute to the toughness threshold")
    void opponentToughnessDoesNotReduceCost() {
        harness.setHand(player1, List.of(new OrysaTideChoreographer()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AvatarOfMight());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast for {1}{U} when total toughness is below 10")
    void noReductionBelowThreshold() {
        harness.setHand(player1, List.of(new OrysaTideChoreographer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Only a 2/2 creature → total toughness 2 < 10, no reduction.
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs {3} less when creatures you control have total toughness 10 or greater")
    void reductionAtThreshold() {
        harness.setHand(player1, List.of(new OrysaTideChoreographer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        // 8/8 + 2/2 = total toughness 10 → {4}{U} becomes {1}{U}.
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Entering the battlefield draws two cards")
    void etbDrawsTwo() {
        harness.setHand(player1, List.of(new OrysaTideChoreographer()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB draw-two trigger

        // Cast Orysa (hand -1), then drew 2 → net +1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Orysa on the stack does not contribute its own toughness")
    void spellDoesNotCountItsOwnToughness() {
        harness.setHand(player1, List.of(new OrysaTideChoreographer()));
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reduction leaves the blue mana requirement intact")
    void reductionDoesNotPayColoredMana() {
        harness.setHand(player1, List.of(new OrysaTideChoreographer()));
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
