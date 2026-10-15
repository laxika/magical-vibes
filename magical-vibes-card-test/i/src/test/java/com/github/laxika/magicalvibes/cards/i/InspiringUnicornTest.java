package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringUnicorn.class, GrizzlyBears.class})
class InspiringUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives creatures you control +1/+1 until end of turn")
    void boostsOwnCreatures() {
        Permanent unicorn = addCreatureReady(player1, new InspiringUnicorn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(unicorn.getEffectivePower()).isEqualTo(3);
        assertThat(unicorn.getEffectiveToughness()).isEqualTo(3);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost an opponent's creature")
    void doesNotBoostOpponentCreatures() {
        addCreatureReady(player1, new InspiringUnicorn());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new InspiringUnicorn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Unicorn that does not attack does not trigger")
    void nonAttackingUnicornDoesNotTrigger() {
        Permanent unicorn = addCreatureReady(player1, new InspiringUnicorn());
        Permanent attacker = addCreatureReady(player1, new InspiringUnicorn());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(unicorn.getEffectivePower()).isEqualTo(3);
        assertThat(unicorn.getEffectiveToughness()).isEqualTo(3);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Two attacking Unicorns each boost the team")
    void multipleAttackTriggersStack() {
        Permanent first = addCreatureReady(player1, new InspiringUnicorn());
        Permanent second = addCreatureReady(player1, new InspiringUnicorn());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost applies to creatures present at resolution, not later arrivals")
    void creaturesAreDeterminedAtResolution() {
        Permanent attacker = addCreatureReady(player1, new InspiringUnicorn());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        Permanent beforeResolution = addCreatureReady(player1, new InspiringUnicorn());

        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new InspiringUnicorn());

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent attacker = addCreatureReady(player1, new InspiringUnicorn());
        Permanent teammate = addCreatureReady(player1, new InspiringUnicorn());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());

        resolveAllTriggers();

        assertThat(teammate.getEffectivePower()).isEqualTo(3);
        assertThat(teammate.getEffectiveToughness()).isEqualTo(3);
    }
}
