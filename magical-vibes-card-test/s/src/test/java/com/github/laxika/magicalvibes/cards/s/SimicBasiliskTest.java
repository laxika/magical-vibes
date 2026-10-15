package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosIckspitter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicBasilisk.class, GrizzlyBears.class, GiantSpider.class, AzoriusSignet.class,
        RakdosIckspitter.class})
class SimicBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        Permanent basilisk = harness.enterBattlefieldAndReturn(player1, new SimicBasilisk());

        assertThat(basilisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Graft may move a +1/+1 counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent basilisk = harness.enterBattlefieldAndReturn(player1, new SimicBasilisk());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(basilisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent basilisk = harness.enterBattlefieldAndReturn(player1, new SimicBasilisk());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(basilisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft may move a counter onto an opponent's creature that enters")
    void graftMovesCounterOntoOpponentsEnteringCreature() {
        Permanent basilisk = harness.enterBattlefieldAndReturn(player1, new SimicBasilisk());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(basilisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft does not trigger when a noncreature enters")
    void graftDoesNotTriggerForNoncreatureEntering() {
        Permanent basilisk = harness.enterBattlefieldAndReturn(player1, new SimicBasilisk());
        Permanent signet = harness.enterBattlefieldAndReturn(player1, new AzoriusSignet());

        assertThat(basilisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(signet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Grants the targeted creature its combat-damage destruction ability until end of turn")
    void grantsCombatDamageDestructionUntilEndOfTurn() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateAbility(basilisk, attacker);

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        resolveBlockedCombat(player1, attacker, player2, blocker);

        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The granted destruction ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateAbility(basilisk, attacker);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        resolveBlockedCombat(player1, attacker, player2, blocker);

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The activated ability can target an opponent's creature with a +1/+1 counter")
    void grantsCombatDamageDestructionToOpponentCreature() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent blocker = addCreatureReady(player1, new GiantSpider());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateAbility(basilisk, attacker);
        resolveBlockedCombat(player2, attacker, player1, blocker);

        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("The activated ability does nothing if its target loses its counter before resolution")
    void targetMustStillHaveCounterOnResolution() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateAbilityWithoutResolution(basilisk, attacker);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        resolveBlockedCombat(player1, attacker, player2, blocker);

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int basiliskIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basilisk);
        assertThatThrownBy(() -> harness.activateAbility(player1, basiliskIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with a +1/+1 counter")
    void cannotTargetNoncreatureWithCounter() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        signet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int basiliskIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basilisk);
        assertThatThrownBy(() -> harness.activateAbility(player1, basiliskIndex, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Losing the counter after resolution does not remove the granted ability, even if its source dies")
    void grantedAbilitySurvivesCounterLossAndLethalCombatDamage() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateAbility(basilisk, attacker);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        resolveBlockedCombat(player1, attacker, player2, blocker);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Simic Basilisk can grant the ability to itself")
    void canGrantAbilityToItself() {
        Permanent basilisk = addReadyBasilisk(player1);
        activateAbility(basilisk, basilisk);

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        resolveBlockedCombat(player1, basilisk, player2, blocker);

        harness.assertOnBattlefield(player1, "Simic Basilisk");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Granting the ability to one creature does not grant it to other creatures")
    void doesNotDestroyCreaturesDamagedByAnotherCreature() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        activateAbility(basilisk, basilisk);

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        resolveBlockedCombat(player1, attacker, player2, blocker);

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The granted ability does not trigger for noncombat damage")
    void noncombatDamageDoesNotScheduleDestruction() {
        Permanent basilisk = addReadyBasilisk(player1);
        Permanent ickspitter = addCreatureReady(player1, new RakdosIckspitter());
        ickspitter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        activateAbility(basilisk, ickspitter);

        int ickspitterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ickspitter);
        harness.activateAbility(player1, ickspitterIndex, null, spider.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    private Permanent addReadyBasilisk(Player player) {
        Permanent basilisk = harness.enterBattlefieldAndReturn(player, new SimicBasilisk());
        basilisk.setSummoningSick(false);
        return basilisk;
    }

    private void resolveBlockedCombat(Player activePlayer, Permanent attacker,
                                      Player defendingPlayer, Permanent blocker) {
        int attackerIndex = gd.playerBattlefields.get(activePlayer.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(defendingPlayer.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(activePlayer, List.of(attackerIndex));
        gs.declareBlockers(gd, defendingPlayer,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat(activePlayer);
        resolveAllTriggers();
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }

    private void activateAbilityWithoutResolution(Permanent basilisk, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basiliskIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basilisk);
        harness.activateAbility(player1, basiliskIndex, null, target.getId());
    }

    private void activateAbility(Permanent basilisk, Permanent target) {
        activateAbilityWithoutResolution(basilisk, target);
        harness.passBothPriorities();
    }
}
