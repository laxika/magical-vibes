package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BattlefieldMedic;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.l.LavamancersSkill;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoubtlessOne.class, BattlefieldMedic.class, GlorySeeker.class, LavamancersSkill.class})
class DoubtlessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Clerics on the battlefield")
    void powerAndToughnessCountBattlefieldClerics() {
        Permanent doubtlessOne = addCreatureReady(player1, new DoubtlessOne());

        assertThat(gqs.getEffectivePower(gd, doubtlessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, doubtlessOne)).isEqualTo(1);

        addCreatureReady(player1, new BattlefieldMedic());
        addCreatureReady(player2, new BattlefieldMedic());

        assertThat(gqs.getEffectivePower(gd, doubtlessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, doubtlessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains life equal to damage dealt")
    void gainsLifeEqualToDamageDealt() {
        addAttacker(new DoubtlessOne());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Life gain matches the damage dealt as Doubtless One grows")
    void lifeGainMatchesDamageDealt() {
        addAttacker(new DoubtlessOne());
        addCreatureReady(player1, new BattlefieldMedic());
        addCreatureReady(player2, new BattlefieldMedic());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Gains life from noncombat damage dealt to a creature")
    void gainsLifeFromNoncombatDamageToCreature() {
        Permanent doubtlessOne = addCreatureReady(player1, new DoubtlessOne());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LavamancersSkill());
        aura.setAttachedTo(doubtlessOne.getId());
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Still gains life when it deals damage before dying in combat")
    void gainsLifeWhenItDiesAfterDealingDamage() {
        addAttacker(new DoubtlessOne());
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        harness.assertInGraveyard(player1, "Doubtless One");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent addAttacker(DoubtlessOne card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
