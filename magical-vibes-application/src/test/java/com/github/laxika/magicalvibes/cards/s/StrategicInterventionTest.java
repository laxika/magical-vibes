package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrategicIntervention.class, GrizzlyBears.class})
class StrategicInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking alone gets +1/+1 and can tap a defending creature")
    void attacksAloneBoostsAndTapsDefendingCreature() {
        harness.addToBattlefield(player1, new StrategicIntervention());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, defender.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(defender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap target may be declined")
    void tapTargetMayBeDeclined() {
        harness.addToBattlefield(player1, new StrategicIntervention());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(defender.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Neither ability triggers when more than one creature attacks")
    void doesNotTriggerWhenNotAlone() {
        harness.addToBattlefield(player1, new StrategicIntervention());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(defender.isTapped()).isFalse();
    }
}
