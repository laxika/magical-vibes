package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrategicIntervention.class, GrizzlyBears.class, RayOfCommand.class})
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

    @Test
    @DisplayName("The lone attacker gets the boost when the defending player has no creatures")
    void boostsAttackerWithoutLegalTapTargets() {
        harness.addToBattlefield(player1, new StrategicIntervention());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonattacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attacker still gets the boost if an opponent gains control in response")
    void boostsAttackerAfterControlChanges() {
        harness.addToBattlefield(player1, new StrategicIntervention());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1));
            harness.handlePermanentChosen(player1, player1.getId());
            harness.castAndResolveInstant(player2, 0, attacker.getId());
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(defender.isTapped()).isFalse();
    }
}
