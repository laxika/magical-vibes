package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.f.FreewindFalcon;
import com.github.laxika.magicalvibes.cards.u.UktabiOrangutan;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindShear.class, Archangel.class, DarajaGriffin.class, FreewindFalcon.class,
        UktabiOrangutan.class})
class WindShearTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking flyers get -2/-2 and lose flying")
    void debuffsAttackingFlyersAndRemovesFlying() {
        Permanent attackingFlyer = addCreatureReady(player2, new Archangel());
        attackingFlyer.setAttacking(true);
        Permanent nonAttackingFlyer = addCreatureReady(player2, new DarajaGriffin());
        Permanent attackingGround = addCreatureReady(player2, new UktabiOrangutan());
        attackingGround.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new WindShear(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(attackingFlyer.getEffectivePower()).isEqualTo(3);
        assertThat(attackingFlyer.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attackingFlyer, Keyword.FLYING)).isFalse();

        assertThat(nonAttackingFlyer.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttackingFlyer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonAttackingFlyer, Keyword.FLYING)).isTrue();

        assertThat(attackingGround.getEffectivePower()).isEqualTo(2);
        assertThat(attackingGround.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures reduced to 0 toughness die")
    void killsSmallAttackingFlyers() {
        Permanent attackingFlyer = addCreatureReady(player2, new FreewindFalcon());
        attackingFlyer.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new WindShear(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Freewind Falcon");
    }

    @Test
    @DisplayName("Creatures that become attacking flyers later are unaffected")
    void doesNotAffectLaterAttackingFlyers() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new WindShear(), "{2}{G}");
        harness.passBothPriorities();

        Permanent laterAttackingFlyer = addCreatureReady(player2, new Archangel());
        laterAttackingFlyer.setAttacking(true);

        assertThat(laterAttackingFlyer.getEffectivePower()).isEqualTo(5);
        assertThat(laterAttackingFlyer.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, laterAttackingFlyer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent attackingFlyer = addCreatureReady(player1, new Archangel());
        attackingFlyer.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new WindShear(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(attackingFlyer.getEffectivePower()).isEqualTo(3);
        assertThat(attackingFlyer.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attackingFlyer, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attackingFlyer.getEffectivePower()).isEqualTo(5);
        assertThat(attackingFlyer.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, attackingFlyer, Keyword.FLYING)).isTrue();
    }
}
