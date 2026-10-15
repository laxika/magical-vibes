package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigiledBehemoth.class, GrizzlyBears.class, Terminate.class})
class SigiledBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — the Behemoth attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent behemoth = addCreatureReady(player1, new SigiledBehemoth());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new SigiledBehemoth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        Permanent behemoth = addCreatureReady(player1, new SigiledBehemoth());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new SigiledBehemoth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Sigiled Behemoth"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each exalted source boosts the lone attacker independently")
    void multipleSourcesBoostLoneAttacker() {
        Permanent attacker = addCreatureReady(player1, new SigiledBehemoth());
        Permanent other = addCreatureReady(player1, new SigiledBehemoth());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentsAttackerDoesNotReceiveBoost() {
        Permanent defendingBehemoth = addCreatureReady(player1, new SigiledBehemoth());
        Permanent attacker = addCreatureReady(player2, new SigiledBehemoth());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, defendingBehemoth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, defendingBehemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted still boosts the attacker after its source is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent source = addCreatureReady(player1, new SigiledBehemoth());
        Permanent attacker = addCreatureReady(player1, new SigiledBehemoth());
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(player1, List.of(1));
        harness.castAndResolveInstant(player2, 0, source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
    }
}
