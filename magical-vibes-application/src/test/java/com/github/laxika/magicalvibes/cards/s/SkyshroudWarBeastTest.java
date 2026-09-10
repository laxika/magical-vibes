package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.d.DominatingLicid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyshroudWarBeast.class, AdarkarWastes.class, Forest.class, DominatingLicid.class})
class SkyshroudWarBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness are zero when the opponent controls no nonbasic lands")
    void isZeroZeroWithNoOpponentNonbasicLands() {
        harness.addToBattlefield(player2, new Forest());

        Permanent warBeast = harness.enterBattlefieldAndReturn(player1, new SkyshroudWarBeast());

        assertThat(gqs.getEffectivePower(gd, warBeast)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isZero();
    }

    @Test
    @DisplayName("Power and toughness equal the opponent's nonbasic land count")
    void powerAndToughnessCountOpponentNonbasicLands() {
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new AdarkarWastes());

        Permanent warBeast = harness.enterBattlefieldAndReturn(player1, new SkyshroudWarBeast());

        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness update as the opponent's nonbasic lands change")
    void updatesDynamically() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());

        Permanent warBeast = harness.enterBattlefieldAndReturn(player1, new SkyshroudWarBeast());

        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isEqualTo(1);

        harness.addToBattlefield(player2, new AdarkarWastes());
        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(firstLand);
        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chosen opponent remains fixed after control of Skyshroud War Beast changes")
    void chosenOpponentRemainsAfterControlChange() {
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player1, new AdarkarWastes());
        Permanent licid = addCreatureReady(player2, new DominatingLicid());
        harness.addMana(player2, ManaColor.BLUE, 3);

        Permanent warBeast = harness.enterBattlefieldAndReturn(player1, new SkyshroudWarBeast());

        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(2);

        int licidIndex = gd.playerBattlefields.get(player2.getId()).indexOf(licid);
        harness.activateAbility(player2, licidIndex, null, warBeast.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(warBeast);
        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isEqualTo(2);
    }
}
