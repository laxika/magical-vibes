package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.s.ShardVolley;
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

@CardUsed({Forfend.class, BallyrushBanneret.class, ShardVolley.class, Mutavault.class})
class ForfendTest extends BaseCardTest {

    private void castForfend() {
        harness.castFromHand(player1, new Forfend(), "{1}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prevents combat damage to both players' creatures")
    void preventsCombatDamageToBothCreatures() {
        castForfend();
        Permanent attacker = addCreatureReady(player1, new BallyrushBanneret());
        Permanent blocker = addCreatureReady(player2, new BallyrushBanneret());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Ballyrush Banneret");
        harness.assertOnBattlefield(player2, "Ballyrush Banneret");
    }

    @Test
    @DisplayName("Does not prevent combat damage to players")
    void doesNotPreventCombatDamageToPlayers() {
        castForfend();
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BallyrushBanneret());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents spell damage to a creature")
    void preventsSpellDamageToCreature() {
        Permanent creature = addCreatureReady(player2, new BallyrushBanneret());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        castForfend();

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithSacrifice(player1, 0, creature.getId(), land.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Ballyrush Banneret");
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new BallyrushBanneret());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        castForfend();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithSacrifice(player1, 0, creature.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ballyrush Banneret");
    }
}
