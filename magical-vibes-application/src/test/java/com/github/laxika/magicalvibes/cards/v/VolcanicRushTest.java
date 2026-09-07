package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicRush.class, GrizzlyBears.class})
class VolcanicRushTest extends BaseCardTest {

    @Test
    void boostsAttackingCreaturesAndGivesThemTrample() {
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());
        attackingCreature.setAttacking(true);
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingAttacker = addCreatureReady(player2, new GrizzlyBears());
        opposingAttacker.setAttacking(true);

        castVolcanicRush();

        assertThat(attackingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(attackingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opposingAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(opposingAttacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bystander.getEffectivePower()).isEqualTo(2);
        assertThat(bystander.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void effectWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castVolcanicRush();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void castVolcanicRush() {
        harness.setHand(player1, java.util.List.of(new VolcanicRush()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
    }
}
