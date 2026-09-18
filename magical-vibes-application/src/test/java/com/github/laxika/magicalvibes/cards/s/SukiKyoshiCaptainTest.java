package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SukiKyoshiCaptain.class, GorillaWarrior.class, GrizzlyBears.class})
class SukiKyoshiCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Other Warriors you control get +1/+1")
    void buffsOtherWarriorsYouControl() {
        harness.addToBattlefield(player1, new SukiKyoshiCaptain());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonWarrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonWarrior)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentWarrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentWarrior)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability gives double strike to attacking Warriors you control")
    void grantsDoubleStrikeToAttackingWarriors() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiCaptain());
        Permanent attackingWarrior = addCreatureReady(player1, new GorillaWarrior());
        Permanent nonAttackingWarrior = addCreatureReady(player1, new GorillaWarrior());
        Permanent attackingNonWarrior = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GorillaWarrior());
        suki.setAttacking(true);
        attackingWarrior.setAttacking(true);
        attackingNonWarrior.setAttacking(true);
        opponentAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, suki, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackingWarrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttackingWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attackingNonWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        gd.interaction.clearAwaitingInput();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

}
