package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormentorsHelm.class, AxgardCavalry.class, TyvarKell.class})
class TormentorsHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {1} attaches to target creature")
    void equipAttaches() {
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Becoming blocked makes the defending player take 1 damage")
    void blockedCreatureDamagesDefendingPlayer() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new AxgardCavalry());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger fires only once when multiple creatures block")
    void multipleBlockersTriggerOnce() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent firstBlocker = addCreatureReady(player2, new AxgardCavalry());
        Permanent secondBlocker = addCreatureReady(player2, new AxgardCavalry());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(creature)),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(creature))));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An unattached Helm does not trigger")
    void unattachedHelmDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        addCreatureReady(player1, new TormentorsHelm());
        Permanent blocker = addCreatureReady(player2, new AxgardCavalry());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The blocked creature still deals damage after leaving the battlefield")
    void triggerSurvivesCreatureLeaving() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new AxgardCavalry());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Removing the attacked planeswalker does not prevent damage to its controller")
    void triggerSurvivesAttackedPlaneswalkerLeaving() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new AxgardCavalry());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TyvarKell());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        creature.setAttacking(true);
        creature.setAttackTarget(planeswalker.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, planeswalker));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Removing the Helm does not stop its pending trigger")
    void triggerSurvivesHelmLeaving() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new AxgardCavalry());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, helm));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpposingCreature() {
        addCreatureReady(player1, new TormentorsHelm());
        Permanent creature = addCreatureReady(player2, new AxgardCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        addCreatureReady(player1, new TormentorsHelm());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reequipping moves the boost to the new creature")
    void reequippingMovesBoost() {
        Permanent helm = addCreatureReady(player1, new TormentorsHelm());
        Permanent first = addCreatureReady(player1, new AxgardCavalry());
        Permanent second = addCreatureReady(player1, new AxgardCavalry());
        helm.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private void setLifeTotals(int player1Life, int player2Life) {
        harness.setLife(player1, player1Life);
        harness.setLife(player2, player2Life);
    }
}
