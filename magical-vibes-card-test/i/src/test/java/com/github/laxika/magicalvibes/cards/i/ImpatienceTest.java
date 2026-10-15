package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, GiantGrowth.class, Impatience.class})
class ImpatienceTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("End step deals 2 damage to the active player who didn't cast a spell")
    void dealsDamageWhenNoSpellCast() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("No damage when the active player cast a spell this turn")
    void noDamageWhenSpellCast() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage is dealt to the end-step player, not Impatience's controller")
    void damageHitsEndStepPlayerNotController() {
        // Opponent controls Impatience; it still burns the active player on their end step.
        harness.addToBattlefield(player2, new Impatience());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No damage if the end-step player casts a spell before the trigger resolves")
    void noDamageWhenSpellCastAfterTrigger() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);

        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No damage if the end-step player casts a spell before the trigger resolves")
    void noDamageWhenSpellCastAfterTriggerUpstreamReview() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);

        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each player's end step can trigger Impatience")
    void triggersDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepAndResolve(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another player's spell does not prevent damage to the active player")
    void opponentsSpellDoesNotPreventDamage() {
        harness.addToBattlefield(player1, new Impatience());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each copy deals its own damage")
    void multipleCopiesDealDamage() {
        harness.addToBattlefield(player1, new Impatience());
        harness.addToBattlefield(player2, new Impatience());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting Impatience itself prevents its trigger that turn")
    void castingImpatiencePreventsItsTrigger() {
        harness.setHand(player1, List.of(new Impatience()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

}
