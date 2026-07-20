package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BindingMummy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InOketrasNameTest extends BaseCardTest {

    private Permanent byName(java.util.UUID playerId, String name) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Zombies get +2/+1 and other creatures get +1/+1")
    void boostsZombiesAndOtherCreatures() {
        harness.addToBattlefield(player1, new BindingMummy());   // Zombie
        harness.addToBattlefield(player1, new GrizzlyBears());    // non-Zombie creature
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent zombie = byName(player1.getId(), "Binding Mummy");
        assertThat(zombie.getPowerModifier()).isEqualTo(2);
        assertThat(zombie.getToughnessModifier()).isEqualTo(1);

        Permanent bears = byName(player1.getId(), "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new BindingMummy());
        harness.addToBattlefield(player2, new BindingMummy());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        for (Permanent p : gd.playerBattlefields.get(player2.getId())) {
            assertThat(p.getPowerModifier()).isEqualTo(0);
            assertThat(p.getToughnessModifier()).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new BindingMummy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            assertThat(p.getPowerModifier()).isEqualTo(0);
            assertThat(p.getToughnessModifier()).isEqualTo(0);
        }
    }
}
