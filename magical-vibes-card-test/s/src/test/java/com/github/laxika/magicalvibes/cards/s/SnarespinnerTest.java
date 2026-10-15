package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Snarespinner.class, AlpineWatchdog.class, ConcordiaPegasus.class})
class SnarespinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying gives Snarespinner +2/+0 until end of turn")
    void blocksFlyingCreatureBoosts() {
        Permanent attacker = addReadyCreature(player1, true);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isEqualTo(2);
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking a creature without flying does not boost Snarespinner")
    void blocksNonFlyingCreatureDoesNothing() {
        Permanent attacker = addReadyCreature(player1, false);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isZero();
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked by a creature with flying does not trigger Snarespinner")
    void becomesBlockedDoesNothing() {
        Permanent snarespinner = addReadySnarespinner(player1);
        snarespinner.setAttacking(true);
        addReadyCreature(player2, true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isZero();
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The bonus still resolves after the blocked creature loses flying")
    void flyingLostAfterBlockingDoesNotPreventBoost() {
        Permanent attacker = addReadyCreature(player1, true);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(snarespinner.getPowerModifier()).isZero();
        attacker.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isEqualTo(2);
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The bonus still resolves after the blocked creature leaves combat")
    void attackerRemovedAfterBlockingDoesNotPreventBoost() {
        Permanent attacker = addReadyCreature(player1, true);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isEqualTo(2);
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blocking bonus lasts through the end step and expires on the next turn")
    void boostExpiresAtEndOfTurn() {
        Permanent attacker = addReadyCreature(player1, true);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(snarespinner.getPowerModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(snarespinner.getPowerModifier()).isZero();
        assertThat(snarespinner.getToughnessModifier()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(snarespinner);
    }

    private Permanent addReadySnarespinner(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Snarespinner());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, boolean flying) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player,
                flying ? new ConcordiaPegasus() : new AlpineWatchdog());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
