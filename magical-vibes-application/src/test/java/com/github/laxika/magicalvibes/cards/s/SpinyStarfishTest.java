package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinyStarfish.class, Shock.class})
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
    @DisplayName("Creating a regeneration shield without using it creates no Starfish token")
    void unusedRegenerationShieldCreatesNoToken() {
        Permanent starfish = addReadyStarfish(player1);
        activateRegeneration(starfish);

        advanceToEndStepAndResolve();

        assertThat(countStarfishTokens()).isZero();
    }

    @Test
    @DisplayName("{U} grants a regeneration shield")
    void activatedAbilityGrantsShield() {
        Permanent starfish = addReadyStarfish(player1);
        activateRegeneration(starfish);

        assertThat(gd.stack).isEmpty();
        assertThat(starfish.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability triggers at the beginning of an opponent's end step")
    void triggersAtOpponentEndStep() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);
        shockStarfish(starfish);

        advanceToEndStepAndResolve(player2);

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("The end-step trigger uses the regeneration count if Starfish leaves before resolution")
    void usesLastKnownRegenerationCountAfterLeavingBattlefield() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);
        shockStarfish(starfish);

        queueEndStepTrigger(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, starfish.getId());

        harness.assertNotOnBattlefield(player1, "Spiny Starfish");
        resolveAllTriggers();

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    /** Player 2 Shocks the Starfish, which is lethal to its 0/1 body and consumes a shield. */
    private void shockStarfish(Permanent starfish) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, starfish.getId());
    }

    private void advanceToEndStepAndResolve() {
        advanceToEndStepAndResolve(player1);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        queueEndStepTrigger(activePlayer);
        resolveAllTriggers();
    }

    private void queueEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void activateRegeneration(Permanent starfish) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int starfishIndex = gd.playerBattlefields.get(player1.getId()).indexOf(starfish);
        harness.activateAbility(player1, starfishIndex, null, null);
        harness.passBothPriorities();
    }

    private long countStarfishTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Starfish"))
                .count();
    }

    private Permanent addReadyStarfish(Player player) {
        return addCreatureReady(player, new SpinyStarfish());
    }
}
