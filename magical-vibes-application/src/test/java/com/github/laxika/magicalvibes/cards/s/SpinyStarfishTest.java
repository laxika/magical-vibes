package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpinyStarfishTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerating once creates one Starfish token at the end step")
    void oneRegenerationCreatesOneToken() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);

        shockStarfish(starfish);

        harness.assertOnBattlefield(player1, "Spiny Starfish");
        assertThat(starfish.getTimesRegeneratedThisTurn()).isEqualTo(1);
        advanceToEndStepAndResolve();

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerating twice creates two Starfish tokens at the end step")
    void twoRegenerationsCreateTwoTokens() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(2);

        shockStarfish(starfish);
        shockStarfish(starfish);

        harness.assertOnBattlefield(player1, "Spiny Starfish");
        advanceToEndStepAndResolve();

        assertThat(countStarfishTokens()).isEqualTo(2);
    }

    @Test
    @DisplayName("No trigger at the end step when it did not regenerate this turn")
    void noTriggerWithoutRegeneration() {
        addReadyStarfish(player1);

        advanceToEndStepAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(countStarfishTokens()).isZero();
    }

    @Test
    @DisplayName("{U} grants a regeneration shield")
    void activatedAbilityGrantsShield() {
        Permanent starfish = addReadyStarfish(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(starfish.getRegenerationShield()).isEqualTo(1);
    }

    /** Player 2 Shocks the Starfish, which is lethal to its 0/1 body and consumes a shield. */
    private void shockStarfish(Permanent starfish) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, starfish.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStepAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step (queues any trigger)
        harness.passBothPriorities(); // resolve the trigger
    }

    private long countStarfishTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Starfish"))
                .count();
    }

    private Permanent addReadyStarfish(Player player) {
        Permanent perm = new Permanent(new SpinyStarfish());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
