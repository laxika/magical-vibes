package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgateBladeAssassin.class})
class AgateBladeAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked attack still drains life without dealing combat damage to the player")
    void blockedAttackStillDrainsLife() {
        addCreatureReady(player1, new AgateBladeAssassin());
        addCreatureReady(player2, new AgateBladeAssassin());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 1);
    }

    @Test
    @DisplayName("Each attacking Assassin triggers independently")
    void multipleAttackersEachDrainLife() {
        addCreatureReady(player1, new AgateBladeAssassin());
        addCreatureReady(player1, new AgateBladeAssassin());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 4);
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves the battlefield")
    void attackTriggerSurvivesSourceRemoval() {
        Permanent assassin = addCreatureReady(player1, new AgateBladeAssassin());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, assassin));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 1);
    }

    @Test
    @DisplayName("When it attacks, the defending player loses 1 life and its controller gains 1 life")
    void attackDrainsDefendingPlayerAndGainsLife() {
        addCreatureReady(player1, new AgateBladeAssassin());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int defendingPlayerLifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(defendingPlayerLifeBefore - 2);
    }

    @Test
    @DisplayName("An attack by the opponent drains its defending player instead")
    void opponentAttackDrainsPlayerBeingAttacked() {
        addCreatureReady(player2, new AgateBladeAssassin());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore + 1);
    }
}
