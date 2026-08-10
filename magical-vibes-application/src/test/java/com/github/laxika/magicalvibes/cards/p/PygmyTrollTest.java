package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PygmyTrollTest extends BaseCardTest {

    @Test
    @DisplayName("When Pygmy Troll becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent troll = addTrollReady(player1);
        troll.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(troll.getPowerModifier()).isEqualTo(1);
        assertThat(troll.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Pygmy Troll is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent troll = addTrollReady(player1);
        troll.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(troll.getPowerModifier()).isZero();
        assertThat(troll.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("{G} grants a regeneration shield")
    void regenerationShield() {
        Permanent troll = addTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves Pygmy Troll from lethal combat damage")
    void shieldSavesFromLethalCombatDamage() {
        Permanent troll = addTrollReady(player1);
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pygmy Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
    }

    private Permanent addTrollReady(Player player) {
        Permanent permanent = new Permanent(new PygmyTroll());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
