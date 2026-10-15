package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.p.PowerOfFire;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({StigmaLasher.class, AngelOfMercy.class, PowerOfFire.class, Snakeform.class})
class StigmaLasherTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player can't gain life after Stigma Lasher deals damage to them")
    void damagedPlayerCantGainLife() {
        addAttackingLasher(player1);

        resolveCombatAndTrigger();
        // 2/2 dealt 2 combat damage.
        harness.assertLife(player2, 18);

        castAngelOfMercy(player2);

        // Life gain was prevented for the rest of the game.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevention persists after Stigma Lasher leaves the battlefield")
    void preventionPersistsAfterLasherLeaves() {
        addAttackingLasher(player1);

        resolveCombatAndTrigger();
        harness.assertLife(player2, 18);

        // Stigma Lasher leaves — the effect is a rest-of-game player state, not a static.
        gd.playerBattlefields.get(player1.getId()).clear();

        castAngelOfMercy(player2);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Undamaged opponent can still gain life")
    void undamagedPlayerCanStillGainLife() {
        addAttackingLasher(player1);

        resolveCombatAndTrigger();
        harness.assertLife(player2, 18);

        // player1 was never damaged by Stigma Lasher, so their life gain is unaffected.
        castAngelOfMercy(player1);

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Wither damages a blocker with counters without preventing its controller's life gain")
    void blockedLasherDealsWitherDamageOnly() {
        addAttackingLasher(player1);
        Permanent blocker = addCreatureReady(player2, new AngelOfMercy());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Angel of Mercy");
        harness.assertInGraveyard(player1, "Stigma Lasher");
        harness.assertLife(player2, 20);

        castAngelOfMercy(player2);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Life gain remains possible until the damage trigger resolves")
    void lifeGainAllowedBeforeTriggerResolves() {
        addAttackingLasher(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 18);

        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.passBothPriorities();
        harness.assertLife(player2, 21);

        resolveAllTriggers();
        castAngelOfMercy(player2);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Noncombat damage also prevents the damaged opponent from gaining life")
    void noncombatDamageLocksOpponentLifeGain() {
        dealNoncombatDamage(player2);
        harness.assertLife(player2, 19);

        castAngelOfMercy(player2);
        harness.assertLife(player2, 19);
        castAngelOfMercy(player1);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Damage to Stigma Lasher's own controller prevents that player's life gain")
    void noncombatDamageLocksControllerLifeGain() {
        dealNoncombatDamage(player1);
        harness.assertLife(player1, 19);

        castAngelOfMercy(player1);
        harness.assertLife(player1, 19);
        castAngelOfMercy(player2);
        harness.assertLife(player2, 23);
    }

    private void dealNoncombatDamage(Player damagedPlayer) {
        Permanent lasher = addCreatureReady(player1, new StigmaLasher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(lasher.getId());

        harness.activateAbility(player1, 0, null, damagedPlayer.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Losing abilities before noncombat damage prevents the life-gain restriction from triggering")
    void losingAbilitiesBeforeDamagePreventsTrigger() {
        Permanent lasher = addCreatureReady(player1, new StigmaLasher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(lasher.getId());
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setLibrary(player2, List.of(new AngelOfMercy()));
        harness.setHand(player2, List.of(new Snakeform()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, lasher.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        castAngelOfMercy(player2);
        harness.assertLife(player2, 22);
    }

    private void addAttackingLasher(Player player) {
        Permanent lasher = addCreatureReady(player, new StigmaLasher());
        lasher.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }

    private void castAngelOfMercy(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB gain life effect
    }
}
