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

        assertThat(gqs.getEffectivePower(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nonAttacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost an opponent's creature")
    void doesNotBoostOpponentCreatures() {
        addCreatureReady(player1, new InspiringUnicorn());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new InspiringUnicorn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Unicorn that does not attack does not trigger")
    void nonAttackingUnicornDoesNotTrigger() {
        Permanent unicorn = addCreatureReady(player1, new InspiringUnicorn());
        Permanent attacker = addCreatureReady(player1, new InspiringUnicorn());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two attacking Unicorns each boost the team")
    void multipleAttackTriggersStack() {
        Permanent first = addCreatureReady(player1, new InspiringUnicorn());
        Permanent second = addCreatureReady(player1, new InspiringUnicorn());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost applies to creatures present at resolution, not later arrivals")
    void creaturesAreDeterminedAtResolution() {
        Permanent attacker = addCreatureReady(player1, new InspiringUnicorn());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        Permanent beforeResolution = addCreatureReady(player1, new InspiringUnicorn());

        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new InspiringUnicorn());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
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

        assertThat(gqs.getEffectivePower(gd, teammate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, teammate)).isEqualTo(3);
    }
}
