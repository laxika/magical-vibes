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
        declareAttackers(List.of(0, 1));

        castStampede();

        assertThat(p1Attacker.getEffectivePower()).isEqualTo(3);
        assertThat(p1Attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(p1Attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(secondAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(secondAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(secondAttacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p1Idle.getEffectivePower()).isEqualTo(2);
        assertThat(p1Idle.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stampede effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        castStampede();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
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

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The defending player can cast Stampede to buff opposing attackers only")
    void defendingPlayerBuffsOpposingAttackers() {
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        Permanent idle = addCreatureReady(player2, new BalduvianBears());
        Permanent defender = addCreatureReady(player1, new BalduvianBears());
        declareAttackers(player2, List.of(0));

        castStampede();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(idle.getEffectivePower()).isEqualTo(2);
        assertThat(idle.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(defender.getEffectivePower()).isEqualTo(2);
        assertThat(defender.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stampede with no attackers resolves without affecting later attackers")
    void noAttackersAtResolution() {
        Permanent creature = addCreatureReady(player1, new BalduvianBears());

        castStampede();
        declareAttackers(List.of(0));

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Stampede");
    }

    @Test
    @DisplayName("Two Stampedes stack their power boosts until end of turn")
    void multipleStampedesStack() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        declareAttackers(List.of(0));

        castStampede();
        castStampede();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    private void castStampede() {
        harness.castFromHand(player1, new Stampede(), "{1}{G}{G}");
        harness.passBothPriorities();
    }
}
