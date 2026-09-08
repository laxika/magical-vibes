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
    @DisplayName("Reduces the morph cost paid to turn a creature face up")
    void reducesMorphCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);

        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player1, "Daru Lancer");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lancer));

        assertThat(lancer.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Does not reduce the cost of casting a creature face down")
    void doesNotReduceFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce an opponent's morph cost")
    void doesNotReduceOpponentMorphCost() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.setHand(player2, List.of(new DaruLancer()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player2, "Daru Lancer");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(lancer)))
                .isInstanceOf(IllegalStateException.class);
    }
}
