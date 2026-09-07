package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Kookus.class)
class KookusTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep deals 3 damage and forces attack when no Keeper of Kookus")
    void upkeepPunishesWithoutKeeper() {
        harness.setLife(player1, 20);
        Permanent kookus = addCreatureReady(player1, new Kookus());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(kookus.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @CardUsed(KeeperOfKookus.class)
    @DisplayName("Upkeep penalty does nothing if a Keeper appears before the trigger resolves")
    void upkeepPenaltyRechecksKeeperAtResolution() {
        harness.setLife(player1, 20);
        Permanent kookus = addCreatureReady(player1, new Kookus());

        advanceToUpkeep(player1);
        addCreatureReady(player1, new KeeperOfKookus());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(kookus.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Upkeep penalty requires Kookus to attack when able")
    void upkeepPenaltyRequiresAttackWhenAble() {
        addCreatureReady(player1, new Kookus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @CardUsed(KeeperOfKookus.class)
    @DisplayName("Upkeep does not trigger while controlling Keeper of Kookus")
    void upkeepSkippedWithKeeper() {
        harness.setLife(player1, 20);
        Permanent kookus = addCreatureReady(player1, new Kookus());
        addCreatureReady(player1, new KeeperOfKookus());

        advanceToUpkeep(player1);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(kookus.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @CardUsed(KeeperOfKookus.class)
    @DisplayName("Opponent's Keeper of Kookus does not prevent the upkeep penalty")
    void opponentsKeeperDoesNotHelp() {
        harness.setLife(player1, 20);
        Permanent kookus = addCreatureReady(player1, new Kookus());
        addCreatureReady(player2, new KeeperOfKookus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(kookus.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void pumpAbilityBoostsPower() {
        Permanent kookus = addCreatureReady(player1, new Kookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kookus.getPowerModifier()).isEqualTo(1);
        assertThat(kookus.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent kookus = addCreatureReady(player1, new Kookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(kookus.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kookus.getPowerModifier()).isEqualTo(0);
    }
}
