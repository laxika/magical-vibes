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

    @Test
    @DisplayName("Opponent is chosen during spell resolution before state-based actions")
    void survivesSpellResolutionWithOpponentNonbasicLand() {
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new DominatingLicid());

        harness.castFromHand(player1, new SkyshroudWarBeast(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud War Beast");
        Permanent warBeast = findPermanent(player1, "Skyshroud War Beast");
        assertThat(gqs.getEffectivePower(gd, warBeast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warBeast)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("War Beast dies after resolving when only its controller has nonbasic lands")
    void diesWithoutChosenOpponentNonbasicLands() {
        harness.addToBattlefield(player1, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new SkyshroudWarBeast(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud War Beast");
        harness.assertInGraveyard(player1, "Skyshroud War Beast");
    }

    @Test
    @DisplayName("Losing the chosen opponent's last nonbasic land makes War Beast die")
    void diesWhenLastOpponentNonbasicLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());
        Permanent warBeast = harness.enterBattlefieldAndReturn(player1, new SkyshroudWarBeast());

        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(warBeast);
        harness.assertInGraveyard(player1, "Skyshroud War Beast");
    }
}
