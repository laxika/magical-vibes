package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InspiringClericTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB gain life effect")
    void hasEtbEffect() {
        InspiringCleric card = new InspiringCleric();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect effect =
                (GainLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.amount()).isEqualTo(4);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Inspiring Cleric puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castInspiringCleric();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Inspiring Cleric");
    }

    // ===== Resolving creature spell =====

    @Test
    @DisplayName("Resolving puts Inspiring Cleric on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castInspiringCleric();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inspiring Cleric"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Inspiring Cleric");
    }

    // ===== ETB life gain =====

    @Test
    @DisplayName("ETB trigger causes controller to gain 4 life")
    void etbGainsLife() {
        castInspiringCleric();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castInspiringCleric();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castInspiringCleric();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castInspiringCleric() {
        harness.setHand(player1, List.of(new InspiringCleric()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }
}
