package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplinterHamatoYoshi.class, NinjaOfTheDeepHours.class, GrizzlyBears.class})
class SplinterHamatoYoshiTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other Ninjas you control +1/+1")
    void boostsOtherNinjasYouControl() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterHamatoYoshi());
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new NinjaOfTheDeepHours());
        Permanent nonNinja = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingNinja = harness.addToBattlefieldAndReturn(player2, new NinjaOfTheDeepHours());

        assertThat(gqs.getEffectivePower(gd, splinter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, splinter)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonNinja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonNinja)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingNinja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingNinja)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Splinter in tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SplinterHamatoYoshi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent splinter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SplinterHamatoYoshi)
                .findFirst()
                .orElseThrow();
        assertThat(splinter.isTapped()).isTrue();
        assertThat(splinter.isAttacking()).isTrue();
        assertThat(splinter.getAttackTarget()).isEqualTo(player2.getId());
    }
}
