package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloseQuartersTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked creature you control triggers 1 damage to a chosen creature")
    void blockedAllyDealsDamageToChosenCreature() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocked creature you control triggers 1 damage to a chosen player")
    void blockedAllyDealsDamageToChosenPlayer() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A creature controlled by an opponent does not trigger Close Quarters")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addReadyCreature(player2);
        attacker.setAttacking(true);
        addReadyCreature(player1);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
