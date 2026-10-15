package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.ButcherOrgg;
import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TifaMartialArtist.class, Gigantosaurus.class, GrizzlyBears.class, ButcherOrgg.class})
class TifaMartialArtistTest extends BaseCardTest {

    @Test
    @DisplayName("High-power combat damage untaps your creatures and grants an extra combat in the first combat")
    void highPowerCombatDamageUntapsCreaturesAndGrantsExtraCombat() {
        addCreatureReady(player1, new TifaMartialArtist());
        Permanent attacker = addCreatureReady(player1, new Gigantosaurus());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        attacker.setAttacking(true);

        gd.combatPhasesThisTurn = 1;
        resolveCombatDamage();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage from a creature below seven power does not trigger")
    void lowPowerCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new TifaMartialArtist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        attacker.setAttacking(true);

        resolveCombatDamage();

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("High-power combat damage in a later combat untaps creatures without granting another combat")
    void laterCombatDoesNotGrantAnotherCombat() {
        addCreatureReady(player1, new TifaMartialArtist());
        Permanent attacker = addCreatureReady(player1, new Gigantosaurus());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        attacker.setAttacking(true);

        gd.combatPhasesThisTurn = 2;
        resolveCombatDamage();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Melee boosts Tifa once when she attacks an opponent")
    void meleeBoostsTifaWhenAttacking() {
        Permanent tifa = addCreatureReady(player1, new TifaMartialArtist());
        int powerBefore = gqs.getEffectivePower(gd, tifa);
        int toughnessBefore = gqs.getEffectiveToughness(gd, tifa);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Tifa herself triggers the untap ability at exactly seven power")
    void tifaTriggersAtExactlySevenPower() {
        Permanent tifa = addCreatureReady(player1, new TifaMartialArtist());
        tifa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        tifa.setAttacking(true);
        tifa.tap();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(tifa.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two qualifying creatures damaging the same player trigger Tifa only once")
    void qualifyingCreaturesTriggerOncePerDamageEvent() {
        harness.setLife(player2, 100);
        addCreatureReady(player1, new TifaMartialArtist());
        Permanent first = addCreatureReady(player1, new Gigantosaurus());
        Permanent second = addCreatureReady(player1, new Gigantosaurus());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Combat damage on an opponent's turn untaps creatures but grants no extra combat")
    void blockingCreatureDamageDoesNotGrantCombatOnOpponentsTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TifaMartialArtist());
        Permanent orgg = addCreatureReady(player2, new ButcherOrgg());
        orgg.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        orgg.setBlocking(true);
        orgg.addBlockingTarget(0);
        Permanent tappedCreature = addCreatureReady(player2, new GrizzlyBears());
        tappedCreature.tap();
        gd.combatPhasesThisTurn = 1;
        resolveCombat(player1);

        harness.handleCombatDamageAssigned(player2, 1, Map.of(player2.getId(), 7));
        resolveAllTriggers();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    private void resolveCombatDamage() {
        resolveCombat(player1);
        harness.passBothPriorities();
    }
}
