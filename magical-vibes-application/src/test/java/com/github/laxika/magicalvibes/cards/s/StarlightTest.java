package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StarlightTest extends BaseCardTest {

    private void castStarlight() {
        harness.setHand(player1, List.of(new Starlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 3 life for each black creature the target opponent controls")
    void gainsThreeLifePerBlackCreature() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new BogImp());
        harness.addToBattlefield(player2, new BogImp());

        castStarlight();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Only black creatures count; non-black creatures are ignored")
    void ignoresNonBlackCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new BogImp());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castStarlight();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Only the target opponent's black creatures count, not the caster's")
    void ignoresCastersBlackCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BogImp());
        harness.addToBattlefield(player2, new BogImp());

        castStarlight();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Gains no life when the opponent controls no black creatures")
    void gainsNothingWithoutBlackCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castStarlight();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Starlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
