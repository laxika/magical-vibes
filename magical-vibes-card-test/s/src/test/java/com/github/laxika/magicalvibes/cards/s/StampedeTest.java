package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stampede.class, BalduvianBears.class})
class StampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Stampede buffs every attacker and leaves nonattackers unchanged")
    void buffsAllAttackers() {
        Permanent p1Attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent secondAttacker = addCreatureReady(player1, new BalduvianBears());
        Permanent p1Idle = addCreatureReady(player1, new BalduvianBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));

        castStampede();

        assertThat(gqs.getEffectivePower(gd, p1Attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, p1Attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, p1Attacker, Keyword.TRAMPLE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondAttacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secondAttacker, Keyword.TRAMPLE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, p1Idle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, p1Idle, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stampede effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        castStampede();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stampede affects the attacking creatures present when it resolves")
    void affectedAttackersAreLockedInAtResolution() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent nonAttacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        castStampede();

        attacker.setAttacking(false);
        nonAttacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The defending player can cast Stampede to buff opposing attackers only")
    void defendingPlayerBuffsOpposingAttackers() {
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        Permanent idle = addCreatureReady(player2, new BalduvianBears());
        Permanent defender = addCreatureReady(player1, new BalduvianBears());
        declareAttackers(player2, List.of(0));

        castStampede();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, idle, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, defender)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, defender, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stampede with no attackers resolves without affecting later attackers")
    void noAttackersAtResolution() {
        Permanent creature = addCreatureReady(player1, new BalduvianBears());

        castStampede();
        declareAttackers(List.of(0));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Stampede");
    }

    @Test
    @DisplayName("Two Stampedes stack their power boosts until end of turn")
    void multipleStampedesStack() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            castStampede();
            castStampede();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    private void castStampede() {
        harness.castFromHand(player1, new Stampede(), "{1}{G}{G}");
        harness.passBothPriorities();
    }
}
