package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBeastDeathlessPrince.class, GrizzlyBears.class})
class TheBeastDeathlessPrinceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with six stun counters")
    void entersTappedWithStunCounters() {
        TheBeastDeathlessPrince card = new TheBeastDeathlessPrince();
        card.setOwnerId(player1.getId());

        Permanent beast = harness.enterBattlefieldAndReturn(player1, card);

        assertThat(beast.isTapped()).isTrue();
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(6);
    }

    @Test
    @DisplayName("On cast steals, untaps and grants menace and haste to a creature")
    void castTriggerTemporarilyImprovesTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new TheBeastDeathlessPrince()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Draws and removes a stun counter when a creature damages its owner")
    void drawsWhenCreatureDamagesItsOwner() {
        TheBeastDeathlessPrince card = new TheBeastDeathlessPrince();
        card.setOwnerId(player1.getId());
        Permanent beast = harness.enterBattlefieldAndReturn(player1, card);

        GrizzlyBears ownedByPlayer2 = new GrizzlyBears();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent attacker = addCreatureReady(player1, ownedByPlayer2);
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(5);
        assertThat(beast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when a creature damages another player")
    void doesNotTriggerWhenCreatureDamagesAnotherPlayer() {
        TheBeastDeathlessPrince card = new TheBeastDeathlessPrince();
        card.setOwnerId(player1.getId());
        Permanent beast = harness.enterBattlefieldAndReturn(player1, card);

        GrizzlyBears ownedByPlayer1 = new GrizzlyBears();
        ownedByPlayer1.setOwnerId(player1.getId());
        Permanent attacker = addCreatureReady(player1, ownedByPlayer1);
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(6);
        assertThat(beast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One combat-damage ability untaps and draws in the same resolution")
    void untapsAndDrawsInOneResolution() {
        Permanent beast = harness.enterBattlefieldAndReturn(player1, new TheBeastDeathlessPrince());
        GrizzlyBears stolenCreature = new GrizzlyBears();
        stolenCreature.setOwnerId(player2.getId());
        addCreatureReady(player1, stolenCreature).setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(5);
        assertThat(beast.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untaps without stun counters and draws for an opponent's creature")
    void observesOpponentsCreatureDamagingItsOwner() {
        Permanent beast = harness.enterBattlefieldAndReturn(player1, new TheBeastDeathlessPrince());
        beast.setCounterCount(CounterType.STUN, 0);
        GrizzlyBears stolenCreature = new GrizzlyBears();
        stolenCreature.setOwnerId(player1.getId());
        addCreatureReady(player2, stolenCreature).setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(beast.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
    }

    @Test
    @DisplayName("Each creature damaging its owner removes a stun counter and draws")
    void triggersForEachCreatureRatherThanEachDamageBatch() {
        Permanent beast = harness.enterBattlefieldAndReturn(player1, new TheBeastDeathlessPrince());
        for (int i = 0; i < 2; i++) {
            GrizzlyBears stolenCreature = new GrizzlyBears();
            stolenCreature.setOwnerId(player2.getId());
            addCreatureReady(player1, stolenCreature).setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(4);
        assertThat(beast.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Can be cast with no creatures available for the cast trigger")
    void canBeCastWithoutAnAvailableCreatureTarget() {
        harness.setHand(player1, List.of(new TheBeastDeathlessPrince()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent beast = findPermanent(player1, "The Beast, Deathless Prince");
        assertThat(beast.isTapped()).isTrue();
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(6);
    }

    @Test
    @DisplayName("Control, menace and haste from the cast ability expire at end of turn")
    void castAbilityExpiresAtEndOfTurn() {
        GrizzlyBears ownedByPlayer2 = new GrizzlyBears();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, ownedByPlayer2);
        harness.setHand(player1, List.of(new TheBeastDeathlessPrince()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }
}
