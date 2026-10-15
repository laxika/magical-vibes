package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reconnaissance;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicGuardian.class, GrizzlyBears.class})
class AngelicGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control gain indestructible")
    void attackingCreaturesYouControlGainIndestructible() {
        Permanent guardian = addCreatureReady(player1, new AngelicGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new AngelicGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void triggersOnceForMultipleAttackersWithoutGuardianAttacking() {
        Permanent guardian = addCreatureReady(player1, new AngelicGuardian());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2));
            assertThat(gd.stack).hasSize(1);
            assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isFalse();
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void opponentsAttackDoesNotTriggerGuardian() {
        addCreatureReady(player1, new AngelicGuardian());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.stack).isEmpty();
        });

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @CardUsed({Reconnaissance.class})
    void creatureRemovedFromCombatInResponseStillGainsIndestructible() {
        addCreatureReady(player1, new AngelicGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Reconnaissance());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(gd.stack).hasSize(1);
            harness.activateAbility(player1, 2, null, attacker.getId());
            resolveAllTriggers();
        });

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
