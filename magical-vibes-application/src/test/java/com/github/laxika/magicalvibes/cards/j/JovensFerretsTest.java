package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JovensFerrets.class, Roterothopter.class})
class JovensFerretsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Joven's Ferrets +0/+2 until end of turn")
    void attackTriggerBoostsToughness() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ferrets.getPowerModifier()).isZero();
        assertThat(ferrets.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ferrets)).isEqualTo(3);
    }

    @Test
    @DisplayName("The end-of-combat ability does not trigger during blocker declaration")
    void endOfCombatAbilityDoesNotTriggerDuringBlockerDeclaration() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        addCreatureReady(player2, new Roterothopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blocker is tapped at end of combat and skips its next untap step")
    void blockerTappedAndUntapLockedAtEndOfCombat() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Roterothopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger if Joven's Ferrets leaves before end of combat")
    void doesNotTriggerIfSourceLeavesBeforeEndOfCombat() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Roterothopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ferrets));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Nothing is scheduled when Joven's Ferrets goes unblocked")
    void nothingScheduledWhenUnblocked() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
    }
}
