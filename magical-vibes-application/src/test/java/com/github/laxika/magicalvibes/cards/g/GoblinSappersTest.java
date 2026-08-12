package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinSappersTest extends BaseCardTest {

    @Test
    @DisplayName("{R}{R} mode makes the target unblockable and schedules both it and Goblin Sappers")
    void cheapModeSchedulesTargetAndSelf() {
        Permanent sappers = addReady(player1, new GoblinSappers());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, indexOf(player1, sappers), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .extracting(DelayedPermanentAction::permanentId)
                .contains(bears.getId(), sappers.getId());
    }

    @Test
    @DisplayName("{R}{R} mode: both the target and Goblin Sappers are destroyed at end of combat")
    void cheapModeDestroysBothAtEndOfCombat() {
        Permanent sappers = addReady(player1, new GoblinSappers());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, indexOf(player1, sappers), 0, null, bears.getId());
        harness.passBothPriorities();

        advanceThroughEndOfCombat();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Goblin Sappers");
        harness.assertInGraveyard(player1, "Goblin Sappers");
    }

    @Test
    @DisplayName("{R}{R}{R}{R} mode destroys only the target at end of combat, sparing Goblin Sappers")
    void expensiveModeSparesSappers() {
        Permanent sappers = addReady(player1, new GoblinSappers());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, indexOf(player1, sappers), 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();

        advanceThroughEndOfCombat();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Goblin Sappers");
    }

    @Test
    @DisplayName("Cheap mode can target Goblin Sappers itself")
    void cheapModeCanTargetItself() {
        Permanent sappers = addReady(player1, new GoblinSappers());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, indexOf(player1, sappers), 0, null, sappers.getId());
        harness.passBothPriorities();

        assertThat(sappers.isCantBeBlocked()).isTrue();

        advanceThroughEndOfCombat();

        harness.assertNotOnBattlefield(player1, "Goblin Sappers");
        harness.assertInGraveyard(player1, "Goblin Sappers");
    }

    @Test
    @DisplayName("Can't target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent sappers = addReady(player1, new GoblinSappers());
        Permanent enemyBears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID illegalTargetId = enemyBears.getId();
        Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, indexOf(player1, sappers), 0, null, illegalTargetId));
    }

    private void advanceThroughEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
