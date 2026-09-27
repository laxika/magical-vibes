package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HiredMuscle;
import com.github.laxika.magicalvibes.cards.s.Scarmaker;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({RoninCliffrider.class, HiredMuscle.class, Scarmaker.class})
class RoninCliffriderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger deals 1 damage to each defending creature")
    void attackTriggerDamagesDefendingCreatures() {
        addReadyRonin(player1);
        Permanent defendingMuscle = addCreatureReady(player2, new HiredMuscle());
        Permanent ownMuscle = addCreatureReady(player1, new HiredMuscle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingMuscle.getMarkedDamage()).isEqualTo(1);
        assertThat(ownMuscle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger deals no damage")
    void decliningAttackTriggerDealsNoDamage() {
        addReadyRonin(player1);
        Permanent defendingMuscle = addCreatureReady(player2, new HiredMuscle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingMuscle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Bushido gives Ronin Cliffrider +1/+1 when it blocks")
    void bushidoWhenBlocking() {
        addCreatureReady(player1, new HiredMuscle());
        Permanent ronin = addReadyRonin(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido gives Ronin Cliffrider +1/+1 when it becomes blocked")
    void bushidoWhenBecomesBlocked() {
        Permanent ronin = addReadyRonin(player1);
        addCreatureReady(player2, new HiredMuscle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido's bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HiredMuscle());
        Permanent ronin = addReadyRonin(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }

    private Permanent addReadyRonin(Player player) {
        return addCreatureReady(player, new RoninCliffrider());
    }
}
