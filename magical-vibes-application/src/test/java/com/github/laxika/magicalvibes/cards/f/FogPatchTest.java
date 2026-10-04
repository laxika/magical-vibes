package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.StampedeDriver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FogPatch.class, Mossdog.class, Cloudskate.class, StampedeDriver.class})
class FogPatchTest extends BaseCardTest {

    @Test
    void makesAllUnblockedAttackersBlockedAndDealsNoCombatDamage() {
        Permanent firstAttacker = addCreatureReady(player1, new Mossdog());
        Permanent secondAttacker = addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(firstAttacker.isBlockedWithoutBlockers()).isTrue();
        assertThat(secondAttacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    void leavesAlreadyBlockedAttackersBlockedByTheirBlockers() {
        Permanent blockedAttacker = addCreatureReady(player1, new Mossdog());
        Permanent unblockedAttacker = addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(blockedAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(unblockedAttacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Mossdog");
        harness.assertInGraveyard(player2, "Mossdog");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unblockedAttacker);
    }

    @Test
    void makesAnAttackingCreatureWithEvasionBlockedWithoutABlocker() {
        Permanent flyer = addCreatureReady(player1, new Cloudskate());
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(flyer.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    void cannotBeCastOutsideDeclareBlockersStep() {
        addCreatureReady(player1, new Mossdog());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new FogPatch(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void trampleStillDealsDamageWhenFogPatchCreatesNoBlockers() {
        addCreatureReady(player1, new StampedeDriver());
        Permanent attacker = addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        harness.setHand(player1, List.of(new Mossdog()));
        harness.setHand(player2, List.of(new FogPatch()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void attackingPlayerCanCastFogPatchWithoutTargetingTheirAttackers() {
        Permanent attacker = addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        harness.castFromHand(player1, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
        assertThat(attacker.getEffectivePower()).isEqualTo(1);

        resolveCombat();

        harness.assertLife(player2, 20);
    }
}
