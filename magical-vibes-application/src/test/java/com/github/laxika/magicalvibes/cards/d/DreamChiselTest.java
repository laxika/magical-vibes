package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamChisel.class, DaruHealer.class, HillGiant.class})
class DreamChiselTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the cost of a creature cast face down")
    void reducesFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player1, List.of(new DaruHealer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent healer = findPermanent(player1, "Daru Healer");
        assertThat(healer.isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a creature cast face up")
    void doesNotReduceFaceUpCreatureSpellCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce an opponent's face-down creature spell")
    void doesNotReduceOpponentFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player2, List.of(new DaruHealer()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
