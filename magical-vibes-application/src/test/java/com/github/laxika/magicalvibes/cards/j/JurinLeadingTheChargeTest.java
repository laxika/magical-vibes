package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JurinLeadingTheCharge.class, GrizzlyBears.class, JaceBeleren.class})
class JurinLeadingTheChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Jurin boosts attacking creatures by the defending player's creature count")
    void boostsAttackingCreaturesByDefendingCreatureCount() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(jurin.getPowerModifier()).isEqualTo(2);
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(nonAttacker.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Jurin's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Jurin must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        jurin.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void doesNotBoostCreaturesAttackingPlaneswalkers() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1),
                Map.of(0, player2.getId(), 1, jace.getId()));
        resolveAllTriggers();

        assertThat(jurin.getPowerModifier()).isEqualTo(1);
        assertThat(otherAttacker.getPowerModifier()).isZero();
    }

    @Test
    void attackingPlaneswalkerStillBoostsCreaturesAttackingPlayers() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1),
                Map.of(0, jace.getId(), 1, player2.getId()));
        resolveAllTriggers();

        assertThat(jurin.getPowerModifier()).isZero();
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void countsDefendingCreaturesAtResolutionIncludingTappedCreatures() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        addCreatureReady(player2, new GrizzlyBears()).tap();
        resolveAllTriggers();

        assertThat(jurin.getPowerModifier()).isEqualTo(2);
        assertThat(jurin.getToughnessModifier()).isZero();
    }

    @Test
    void otherCreaturesAttackingWithoutJurinDoNotReceiveBoost() {
        addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
    }

    @Test
    void jurinCanAttackTheTurnItEntersTheBattlefield() {
        Permanent jurin = harness.addToBattlefieldAndReturn(player1, new JurinLeadingTheCharge());
        jurin.setSummoningSick(true);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(jurin.isAttacking()).isTrue();
        assertThat(jurin.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void attackTriggerStillBoostsOtherAttackersAfterJurinLeavesBattlefield() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(jurin);
        gd.playerGraveyards.get(player1.getId()).add(jurin.getCard());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void tappedDefenderDoesNotHaveToBlockJurin() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        addCreatureReady(player2, new GrizzlyBears()).tap();
        jurin.setAttacking(true);
        jurin.setAttackTarget(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(jurin.isBlockedThisCombat()).isFalse();
    }

    @Test
    void eachAttackerUsesTheCreatureCountOfThePlayerItAttacks() {
        Player thirdPlayer = new Player(UUID.randomUUID(), "Charlie");
        UUID id = thirdPlayer.getId();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(thirdPlayer, new GrizzlyBears());
        addCreatureReady(thirdPlayer, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1),
                Map.of(0, player2.getId(), 1, thirdPlayer.getId()));
        resolveAllTriggers();

        assertThat(jurin.getPowerModifier()).isEqualTo(1);
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(2);
    }
}
