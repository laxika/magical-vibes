package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirCultElemental;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.cards.s.SpikedPitTrap;
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

@CardUsed({WestgateRegent.class, AirCultElemental.class, PowerWordKill.class, SpikedPitTrap.class})
class WestgateRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they have no card to discard")
    void wardCountersOpponentSpellWhenHandIsEmpty() {
        Permanent regent = addReadyWestgateRegent();

        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, regent.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Power Word Kill");
        harness.assertOnBattlefield(player1, "Westgate Regent");
        harness.assertNotInGraveyard(player1, "Westgate Regent");
    }

    @Test
    @DisplayName("Puts +1/+1 counters on itself equal to combat damage dealt to a player")
    void putsCountersEqualToCombatDamage() {
        Permanent regent = addReadyWestgateRegent();
        regent.setAttacking(true);

        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when blocked and deals no combat damage to a player")
    void doesNotTriggerWhenBlocked() {
        Permanent regent = addReadyWestgateRegent();
        regent.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AirCultElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void discardingPaysWardAndAllowsSpellToResolve() {
        Permanent regent = addReadyWestgateRegent();
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new PowerWordKill(), new AirCultElemental()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, regent.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Air-Cult Elemental");
        harness.assertNotInHand(player2, "Air-Cult Elemental");
        harness.assertInGraveyard(player1, "Westgate Regent");
        harness.assertNotOnBattlefield(player1, "Westgate Regent");
    }

    @Test
    void decliningDiscardCountersSpellAndKeepsCardInHand() {
        Permanent regent = addReadyWestgateRegent();
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new PowerWordKill(), new AirCultElemental()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, regent.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInHand(player2, "Air-Cult Elemental");
        harness.assertNotInGraveyard(player2, "Air-Cult Elemental");
        harness.assertOnBattlefield(player1, "Westgate Regent");
        harness.assertInGraveyard(player2, "Power Word Kill");
    }

    @Test
    void wardDoesNotCounterControllersOwnSpell() {
        Permanent regent = addReadyWestgateRegent();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, regent.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Westgate Regent");
        harness.assertNotOnBattlefield(player1, "Westgate Regent");
    }

    @Test
    void wardCountersOpponentActivatedAbilityWithoutDiscard() {
        Permanent regent = addReadyWestgateRegent();
        harness.addToBattlefield(player2, new SpikedPitTrap());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, null, regent.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Westgate Regent");
        harness.assertInGraveyard(player2, "Spiked Pit Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersMatchDamageWithExistingCounters() {
        Permanent regent = addReadyWestgateRegent();
        regent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        regent.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyWestgateRegent() {
        return addCreatureReady(player1, new WestgateRegent());
    }
}
