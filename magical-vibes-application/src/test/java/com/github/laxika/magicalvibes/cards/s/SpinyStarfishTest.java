package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GuerrillaTactics;
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

@CardUsed({SpinyStarfish.class, GuerrillaTactics.class})
class SpinyStarfishTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerating once creates one Starfish token at the end step")
    void oneRegenerationCreatesOneToken() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);

        damageStarfish(starfish);

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

        damageStarfish(starfish);
        damageStarfish(starfish);

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
        damageStarfish(starfish);

        advanceToEndStepAndResolve(player2);

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("The end-step trigger uses the regeneration count if Starfish leaves before resolution")
    void usesLastKnownRegenerationCountAfterLeavingBattlefield() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);
        damageStarfish(starfish);

        queueEndStepTrigger(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, starfish.getId());

        harness.assertNotOnBattlefield(player1, "Spiny Starfish");
        resolveAllTriggers();

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated regeneration ability saves the creature and creates a token")
    void activatedShieldPreventsDestructionAndCreatesToken() {
        Permanent starfish = addReadyStarfish(player1);
        activateRegeneration(starfish);

        damageStarfish(starfish);

        harness.assertOnBattlefield(player1, "Spiny Starfish");
        assertThat(starfish.isTapped()).isTrue();
        assertThat(starfish.getMarkedDamage()).isZero();
        assertThat(starfish.getRegenerationShield()).isZero();
        advanceToEndStepAndResolve();

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration while the end-step trigger is pending increases the token count")
    void regenerationCountIsEvaluatedAtResolution() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(2);
        damageStarfish(starfish);
        queueEndStepTrigger(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, starfish.getId());
        resolveAllTriggers();

        assertThat(countStarfishTokens()).isEqualTo(2);
    }

    @Test
    @DisplayName("First regeneration after the end step begins does not create a token that end step")
    void firstRegenerationDuringEndStepDoesNotTrigger() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);
        queueEndStepTrigger(player1);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, starfish.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spiny Starfish");
        assertThat(starfish.getTimesRegeneratedThisTurn()).isEqualTo(1);
        assertThat(countStarfishTokens()).isZero();
    }

    @Test
    @DisplayName("Regeneration from a previous turn does not create more tokens")
    void regenerationCountResetsBetweenTurns() {
        Permanent starfish = addReadyStarfish(player1);
        starfish.setRegenerationShield(1);
        damageStarfish(starfish);
        advanceToEndStepAndResolve();
        assertThat(countStarfishTokens()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(starfish.getTimesRegeneratedThisTurn()).isZero();
        advanceToEndStepAndResolve(player2);

        assertThat(countStarfishTokens()).isEqualTo(1);
    }

    /** Player 2 deals lethal damage to the Starfish's 0/1 body, consuming a shield. */
    private void damageStarfish(Permanent starfish) {
        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
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
        return countPermanents(player1, "Starfish");
    }

    private Permanent addReadyStarfish(Player player) {
        return addCreatureReady(player, new SpinyStarfish());
    }
}
