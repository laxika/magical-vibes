package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
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

@CardUsed({PygmyTroll.class, ElvishBerserker.class})
class PygmyTrollTest extends BaseCardTest {

    @Test
    @DisplayName("When Pygmy Troll becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent troll = addCreatureReady(player1, new PygmyTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new ElvishBerserker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(troll.getPowerModifier()).isEqualTo(1);
        assertThat(troll.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Pygmy Troll is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent troll = addCreatureReady(player1, new PygmyTroll());
        troll.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(troll.getPowerModifier()).isZero();
        assertThat(troll.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Pygmy Troll becomes blocked by two creatures, it gets +2/+2")
    void becomesBlockedByMultipleCreaturesGetsOneBoostPerBlocker() {
        Permanent troll = addCreatureReady(player1, new PygmyTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new ElvishBerserker());
        addCreatureReady(player2, new ElvishBerserker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(troll.getPowerModifier()).isEqualTo(2);
        assertThat(troll.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked without a creature does not trigger Pygmy Troll's boost")
    void becomingBlockedWithoutCreatureDoesNotBoost() {
        Permanent troll = addCreatureReady(player1, new PygmyTroll());
        addCreatureReady(player2, new ElvishBerserker());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        harness.inMutationScope(() -> harness.getCombatBlockService()
                .makeAttackingCreatureBlockedWithoutBlockers(gd, troll));
        harness.passBothPriorities();

        assertThat(troll.isBlockedWithoutBlockers()).isTrue();
        assertThat(troll.getPowerModifier()).isZero();
        assertThat(troll.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("{G} grants a regeneration shield")
    void regenerationShield() {
        Permanent troll = addCreatureReady(player1, new PygmyTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves Pygmy Troll from lethal combat damage")
    void shieldSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new PygmyTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new ElvishBerserker());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pygmy Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
    }
}
