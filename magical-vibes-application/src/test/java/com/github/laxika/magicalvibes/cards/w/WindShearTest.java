package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindShearTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking flyers get -2/-2 and lose flying")
    void debuffsAttackingFlyersAndRemovesFlying() {
        Permanent attackingFlyer = addCreatureReady(player2, new SerraAngel());
        attackingFlyer.setAttacking(true);
        Permanent nonAttackingFlyer = addCreatureReady(player2, new SuntailHawk());
        Permanent attackingGround = addCreatureReady(player2, new GrizzlyBears());
        attackingGround.setAttacking(true);

        harness.setHand(player1, List.of(new WindShear()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attackingFlyer.getEffectivePower()).isEqualTo(2);
        assertThat(attackingFlyer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attackingFlyer, Keyword.FLYING)).isFalse();

        assertThat(nonAttackingFlyer.getEffectivePower()).isEqualTo(1);
        assertThat(nonAttackingFlyer.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonAttackingFlyer, Keyword.FLYING)).isTrue();

        assertThat(attackingGround.getEffectivePower()).isEqualTo(2);
        assertThat(attackingGround.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures reduced to 0 toughness die")
    void killsSmallAttackingFlyers() {
        Permanent attackingFlyer = addCreatureReady(player2, new SuntailHawk());
        attackingFlyer.setAttacking(true);

        harness.setHand(player1, List.of(new WindShear()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Suntail Hawk"))).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent attackingFlyer = addCreatureReady(player1, new SerraAngel());
        attackingFlyer.setAttacking(true);

        harness.setHand(player1, List.of(new WindShear()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attackingFlyer.getEffectivePower()).isEqualTo(2);
        assertThat(attackingFlyer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attackingFlyer, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attackingFlyer.getEffectivePower()).isEqualTo(4);
        assertThat(attackingFlyer.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, attackingFlyer, Keyword.FLYING)).isTrue();
    }
}
