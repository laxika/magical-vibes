package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("War Effort")
@CardUsed({WarEffort.class, GrizzlyBears.class, Opalescence.class})
class WarEffortTest extends BaseCardTest {

    @Test
    @CardUsed({WarEffort.class, Opalescence.class})
    @DisplayName("War Effort gets its own power bonus when it becomes a creature")
    void animatedWarEffortBoostsItself() {
        Permanent warEffort = harness.addToBattlefieldAndReturn(player1, new WarEffort());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, warEffort)).isTrue();
        assertThat(gqs.getEffectivePower(gd, warEffort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warEffort)).isEqualTo(4);
    }

    @Test
    @DisplayName("The power bonus does not apply to opposing creatures")
    void doesNotBoostOpposingCreatures() {
        harness.addToBattlefield(player1, new WarEffort());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple attackers create only one Warrior, without retriggering for its entry")
    void multipleAttackersCreateOneToken() {
        harness.addToBattlefield(player1, new WarEffort());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's attack does not create a Warrior")
    void opposingAttackDoesNotTrigger() {
        harness.addToBattlefield(player1, new WarEffort());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Warrior")).isZero();
        assertThat(countPermanents(player2, "Warrior")).isZero();
    }

    @Test
    @DisplayName("Creatures you control get +1/+0")
    void boostsCreaturesYouControl() {
        harness.addToBattlefield(player1, new WarEffort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Warrior token")
    void attackingCreatesWarriorToken() {
        harness.addToBattlefield(player1, new WarEffort());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(attacker.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The Warrior token is sacrificed at the beginning of the next end step")
    void tokenIsSacrificedAtNextEndStep() {
        harness.addToBattlefield(player1, new WarEffort());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Warrior")).isZero();
    }
}
