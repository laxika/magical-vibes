package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoninCliffriderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger deals 1 damage to each defending creature")
    void attackTriggerDamagesDefendingCreatures() {
        addReadyRonin(player1);
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger deals no damage")
    void decliningAttackTriggerDealsNoDamage() {
        addReadyRonin(player1);
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Bushido gives Ronin Cliffrider +1/+1 when it blocks")
    void bushidoWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent ronin = addReadyRonin(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido gives Ronin Cliffrider +1/+1 when it becomes blocked")
    void bushidoWhenBecomesBlocked() {
        Permanent ronin = addReadyRonin(player1);
        ronin.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addReadyRonin(Player player) {
        return addCreatureReady(player, new RoninCliffrider());
    }
}
