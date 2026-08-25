package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({IbHalfheartGoblinTactician.class, GoblinPiker.class, GrizzlyBears.class, Mountain.class})
class IbHalfheartGoblinTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Another Goblin that becomes blocked is sacrificed and damages each creature blocking it")
    void anotherBlockedGoblinIsSacrificedAndDamagesItsBlockers() {
        Permanent goblin = addReady(player1, new GoblinPiker());
        goblin.setAttacking(true);
        Permanent ib = addReady(player1, new IbHalfheartGoblinTactician());
        Permanent firstBlocker = addReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentSnapshot().getId()).isEqualTo(goblin.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ib).doesNotContain(goblin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goblin.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
    }

    @Test
    @DisplayName("Ib does not trigger for itself becoming blocked")
    void doesNotTriggerForIbBecomingBlocked() {
        Permanent ib = addReady(player1, new IbHalfheartGoblinTactician());
        ib.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two Mountains creates two Goblin tokens")
    void sacrificesTwoMountainsToCreateTwoGoblins() {
        Permanent ib = addReady(player1, new IbHalfheartGoblinTactician());
        Permanent firstMountain = addReady(player1, new Mountain());
        Permanent secondMountain = addReady(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ib), 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ib).doesNotContain(firstMountain, secondMountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMountain.getCard(), secondMountain.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().getName().equals("Goblin"))
                .hasSize(2);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
