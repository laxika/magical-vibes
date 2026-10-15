package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PuppetMasterStringPuller.class, GrizzlyBears.class, HermeticStudy.class})
class PuppetMasterStringPullerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking goads a target opponent creature and stops it blocking this turn")
    void attackingGoadsTargetOpponentCreature() {
        Permanent puppet = addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.isGoaded(gd, target)).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(bls.canBlockAttacker(gd, target, puppet, List.of(target))).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Combat damage to Puppet Master's controller does not create a Treasure")
    void combatDamageToControllerDoesNotCreateTreasure() {
        addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        goadTarget(target);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Noncombat damage from a goaded creature does not create a Treasure")
    void noncombatDamageFromGoadedCreatureDoesNotCreateTreasure() {
        addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        goadTarget(target);

        Permanent study = harness.addToBattlefieldAndReturn(player2, new HermeticStudy());
        study.setAttachedTo(target.getId());
        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        harness.activateAbility(player2, creatureIndex, null, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void opponentsGoadedCreatureDamagingAnotherOpponentCreatesTreasure() {
        Player third = addOpponent();
        addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        goadTarget(bear);

        bear.setAttacking(true);
        bear.setAttackTarget(third.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.getLife(third.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(third, "Treasure")).isZero();
    }

    @Test
    void simultaneousCombatDamageCreatesOnlyOneTreasure() {
        addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent goaded = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new PuppetMasterStringPuller());
        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, goaded.getId());
        resolveAllTriggers();
        assertThat(gqs.isGoaded(gd, goaded)).isTrue();

        for (Permanent bear : List.of(goaded, other)) {
            bear.setAttacking(true);
            bear.setAttackTarget(player2.getId());
        }
        gd.playerBattlefields.get(player2.getId()).forEach(p -> p.setAttacking(false));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    private Player addOpponent() {
        UUID id = UUID.randomUUID();
        Player opponent = new Player(id, "Charlie");
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
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-Charlie"), id, "Charlie");
        return opponent;
    }

    private void goadTarget(Permanent target) {
        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
    }
}
