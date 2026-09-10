package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FlowstoneMauler.class)
class FlowstoneMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +1/-1 until end of turn")
    void activatingAbilityBoostsSelf() {
        Permanent mauler = addCreatureReady(player1, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and the boosts stack")
    void repeatedActivationsStack() {
        Permanent mauler = addCreatureReady(player1, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Trample deals excess combat damage after lethal damage to a blocker")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new FlowstoneMauler());
        Permanent blocker = addCreatureReady(player2, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 5, player2.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mauler = addCreatureReady(player1, new FlowstoneMauler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(5);
    }
}
