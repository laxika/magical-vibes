package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamChisel.class, DaruLancer.class})
class DreamChiselTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the cost of casting a creature face down")
    void reducesFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);

        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player1, "Daru Lancer");
        assertThat(lancer.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Does not reduce the morph cost paid to turn a creature face up")
    void doesNotReduceMorphCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player1, "Daru Lancer");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lancer)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lancer.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Does not reduce an opponent's face-down creature spell cost")
    void doesNotReduceOpponentsFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player2, List.of(new DaruLancer()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
