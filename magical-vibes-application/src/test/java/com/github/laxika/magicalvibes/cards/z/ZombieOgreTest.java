package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZombieOgre.class, PowerWordKill.class})
class ZombieOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Does not venture at the end step when no creature died")
    void doesNotVentureWithoutMorbid() {
        harness.addToBattlefield(player1, new ZombieOgre());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Ventures into a dungeon at the end step after a creature dies")
    void venturesAfterCreatureDies() {
        harness.addToBattlefield(player1, new ZombieOgre());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new ZombieOgre());
        killOgre(player2);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void deathAfterEndStepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZombieOgre());
        advanceToEndStep(player1);

        killOgre(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void multipleDeathsCauseOnlyOneVenture() {
        harness.addToBattlefield(player1, new ZombieOgre());
        killOgre(player1);
        killOgre(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Tomb of Annihilation");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void advancesExistingDungeonAndChoosesBranch() {
        harness.addToBattlefield(player1, new ZombieOgre());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        killOgre(player2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mine Tunnels");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    private void killOgre(Player controller) {
        var victim = harness.addToBattlefieldAndReturn(controller, new ZombieOgre());
        harness.setHand(controller, List.of(new PowerWordKill()));
        harness.addMana(controller, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(controller, 0, victim.getId());
        assertThat(gd.playerGraveyards.get(controller.getId()))
                .anyMatch(card -> card instanceof ZombieOgre);
    }
    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
