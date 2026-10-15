package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MsMarvelElasticAlly.class, Forest.class, GrizzlyBears.class})
class MsMarvelElasticAllyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMsMarvel(target);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Unmodified creature combat damage does not draw")
    void unmodifiedCreatureDoesNotDraw() {
        addMsMarvel();
        addReadyAttacker();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Creature with power greater than base power draws")
    void modifiedCreatureDraws() {
        addMsMarvel();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(1);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The combat-damage draw triggers only once each turn")
    void combatDamageDrawsOnlyOnceEachTurn() {
        addMsMarvel();
        Permanent firstAttacker = addReadyAttacker();
        firstAttacker.setPowerModifier(1);
        Permanent secondAttacker = addReadyAttacker();
        secondAttacker.setPowerModifier(1);
        Card firstCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard).doesNotContain(secondCard);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void etbCannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MsMarvelElasticAlly()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The ETB boost enables a controlled creature to draw on combat damage")
    void etbBoostEnablesDraw() {
        Permanent attacker = addReadyAttacker();
        castMsMarvel(attacker);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Ms. Marvel's own increased power can trigger the draw")
    void msMarvelCanTriggerHerOwnDraw() {
        Permanent attacker = addCreatureReady(player1, new MsMarvelElasticAlly());
        attacker.setAttacking(true);
        attacker.setPowerModifier(1);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Power below base power does not trigger the draw")
    void reducedPowerDoesNotDraw() {
        addMsMarvel();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(-1);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("An opponent's increased-power creature does not trigger the draw")
    void opponentCreatureDoesNotDraw() {
        addMsMarvel();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setPowerModifier(1);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Separate combat damage events in one turn still draw only once")
    void laterCombatDamageDoesNotDrawAgain() {
        addMsMarvel();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(1);
        Card firstCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        resolveCombatWith();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("A +1/+1 counter increases power above base power and enables the draw")
    void counterEnablesDraw() {
        addMsMarvel();
        Permanent attacker = addReadyAttacker();
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Nonqualifying damage does not consume the once-per-turn trigger")
    void nonqualifyingDamageDoesNotConsumeTrigger() {
        addMsMarvel();
        Permanent attacker = addReadyAttacker();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatWith();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        attacker.setPowerModifier(1);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombatWith();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    private void addMsMarvel() {
        harness.addToBattlefield(player1, new MsMarvelElasticAlly());
    }

    private void castMsMarvel(Permanent target) {
        harness.setHand(player1, List.of(new MsMarvelElasticAlly()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private Permanent addReadyAttacker() {
        return addReadyAttacker(new GrizzlyBears());
    }

    private Permanent addReadyAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void resolveCombatWith() {
        resolveCombat();
        resolveAllTriggers();
    }
}
