package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServantOfNefarox.class, WalkingCorpse.class})
class ServantOfNefaroxTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new ServantOfNefarox());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Servant attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent servant = addCreatureReady(player1, new ServantOfNefarox());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new ServantOfNefarox());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new ServantOfNefarox());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Servant of Nefarox"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Servant gives a lone attacker its own exalted boost")
    void multipleServantsBoostSameAttacker() {
        Permanent first = addCreatureReady(player1, new ServantOfNefarox());
        Permanent second = addCreatureReady(player1, new ServantOfNefarox());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentsLoneAttackerIsNotBoosted() {
        addCreatureReady(player1, new ServantOfNefarox());
        Permanent attacker = addCreatureReady(player2, new WalkingCorpse());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
