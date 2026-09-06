package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeonardoLeaderInBlue.class, GrizzlyBears.class})
class LeonardoLeaderInBlueTest extends BaseCardTest {

    @Test
    @DisplayName("Normally cast Leonardo does not boost creatures when he enters")
    void normalCastDoesNotBoostCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeonardoLeaderInBlue()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Leonardo boosts creatures when cast for his sneak cost")
    void sneakCastBoostsCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoLeaderInBlue()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        Permanent leonardo = findPermanent(player1, "Leonardo, Leader in Blue");
        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leonardo)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability grants first strike until end of turn")
    void gainsFirstStrikeUntilEndOfTurn() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoLeaderInBlue());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leonardo, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leonardo, Keyword.FIRST_STRIKE)).isFalse();
    }
}
