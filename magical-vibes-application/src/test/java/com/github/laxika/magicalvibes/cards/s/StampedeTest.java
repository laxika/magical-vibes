package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stampede.class, BalduvianBears.class})
class StampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Stampede buffs all attacking creatures (any player) and grants trample")
    void buffsAllAttackers() {
        Permanent p1Attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent p2Attacker = addCreatureReady(player2, new BalduvianBears());
        Permanent p1Idle = addCreatureReady(player1, new BalduvianBears());
        p1Attacker.setAttacking(true);
        p2Attacker.setAttacking(true);

        castStampede();

        assertThat(p1Attacker.getEffectivePower()).isEqualTo(3);
        assertThat(p1Attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(p1Attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p2Attacker.getEffectivePower()).isEqualTo(3);
        assertThat(p2Attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2Attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

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

    private void castStampede() {
        harness.castFromHand(player1, new Stampede(), "{1}{G}{G}");
        harness.passBothPriorities();
    }
}
