package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PretendersClaim.class, FreshVolunteers.class, Forest.class, NicolBolasPlaneswalker.class})
class PretendersClaimTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature becomes blocked, all defending lands are tapped")
    void becomesBlockedTapsDefendingLands() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player1, attacker);
        Permanent ownLand = addLand(player1);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When enchanted creature becomes blocked, every defending land is tapped")
    void becomesBlockedTapsEveryDefendingLand() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player1, attacker);
        addCreatureReady(player2, new FreshVolunteers());
        Permanent firstDefendingLand = addLand(player2);
        Permanent secondDefendingLand = addLand(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(firstDefendingLand.isTapped()).isTrue();
        assertThat(secondDefendingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Becomes-blocked trigger fires once for multiple blockers")
    void becomesBlockedTriggersOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent claim = addClaimAttachedTo(player1, attacker);
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).filteredOn(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Pretender's Claim")
                        && se.getSourcePermanentId().equals(claim.getId()))
                .hasSize(1);

        harness.passBothPriorities();
        assertThat(defendingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unattached Pretender's Claim does not trigger")
    void unattachedClaimDoesNotTrigger() {
        addCreatureReady(player1, new FreshVolunteers());
        addClaim(player1);
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Pretender's Claim"));
    }

    @Test
    @DisplayName("When the enchanted creature attacks a planeswalker, its controller's lands are tapped")
    void becomesBlockedWhenAttackingPlaneswalkerTapsPlaneswalkersControllerLands() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player1, attacker);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Aura controlled by the defender still taps that defender's lands")
    void defenderControlsClaimOnOpponentsAttacker() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player2, attacker);
        Permanent attackingLand = addLand(player1);
        addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isTrue();
        assertThat(attackingLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger still taps lands after the enchanted attacker leaves")
    void attackerLeavesBeforeTriggerResolves() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player1, attacker);
        addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, attacker));
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unblocked enchanted attacker does not tap defending lands")
    void unblockedAttackDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player1, attacker);
        addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Defending lands are tapped even if the attacked planeswalker leaves before resolution")
    void attackedPlaneswalkerLeavesBeforeTriggerResolves() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addClaimAttachedTo(player1, attacker);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        addCreatureReady(player2, new FreshVolunteers());
        Permanent defendingLand = addLand(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, planeswalker));
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isTrue();
    }

    private Permanent addClaim(Player player) {
        Permanent claim = harness.addToBattlefieldAndReturn(player, new PretendersClaim());
        claim.setSummoningSick(false);
        return claim;
    }

    private Permanent addClaimAttachedTo(Player player, Permanent creature) {
        Permanent claim = addClaim(player);
        claim.setAttachedTo(creature.getId());
        return claim;
    }

    private Permanent addLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        land.setSummoningSick(false);
        return land;
    }
}
