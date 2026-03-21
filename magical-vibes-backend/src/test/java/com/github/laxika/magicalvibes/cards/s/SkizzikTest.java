package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.NotKickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkizzikTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has KickerEffect with cost {R}")
    void hasKickerEffect() {
        Skizzik card = new Skizzik();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{R}"));
    }

    @Test
    @DisplayName("Has NotKickedConditionalEffect wrapping SacrificeSelfEffect on END_STEP_TRIGGERED")
    void hasEndStepSacrificeTrigger() {
        Skizzik card = new Skizzik();

        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(NotKickedConditionalEffect.class);
        assertThat(((NotKickedConditionalEffect) card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst()).wrapped())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — sacrificed at end step")
    void castWithoutKickerSacrificedAtEndStep() {
        harness.setHand(player1, List.of(new Skizzik()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(findSkizzik(player1)).isNotNull();

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // End step trigger should be on the stack
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Skizzik");

        // Resolve the sacrifice trigger
        harness.passBothPriorities();

        // Should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skizzik"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Skizzik"));
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — stays on battlefield at end step")
    void castWithKickerStaysOnBattlefield() {
        harness.setHand(player1, List.of(new Skizzik()));
        harness.addMana(player1, ManaColor.RED, 2); // 1 for base cost + 1 for kicker
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(findSkizzik(player1)).isNotNull();
        assertThat(findSkizzik(player1).isKicked()).isTrue();

        // Advance through end step — no trigger should fire since it was kicked
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances past end step (no triggers)

        // Should still be on battlefield (not sacrificed)
        assertThat(findSkizzik(player1)).isNotNull();
        // Should not be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Skizzik"));
    }

    // ===== Helpers =====

    private Permanent findSkizzik(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Skizzik"))
                .findFirst().orElse(null);
    }
}
