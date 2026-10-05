package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.r.RaccoonRallier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MabelHeirToCragflame.class, ManifoldMouse.class, RaccoonRallier.class, BarkformHarvester.class})
class MabelHeirToCragflameTest extends BaseCardTest {

    @Test
    @DisplayName("Other Mice you control get +1/+1")
    void buffsOtherMiceYouControl() {
        Permanent ownMouse = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        Permanent nonMouse = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        Permanent opponentMouse = harness.addToBattlefieldAndReturn(player2, new ManifoldMouse());
        int ownMousePower = gqs.getEffectivePower(gd, ownMouse);
        int ownMouseToughness = gqs.getEffectiveToughness(gd, ownMouse);
        int nonMousePower = gqs.getEffectivePower(gd, nonMouse);
        int opponentMousePower = gqs.getEffectivePower(gd, opponentMouse);

        harness.addToBattlefield(player1, new MabelHeirToCragflame());

        assertThat(gqs.getEffectivePower(gd, ownMouse)).isEqualTo(ownMousePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownMouse)).isEqualTo(ownMouseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonMouse)).isEqualTo(nonMousePower);
        assertThat(gqs.getEffectivePower(gd, opponentMouse)).isEqualTo(opponentMousePower);
    }

    @Test
    @DisplayName("When Mabel enters, Cragflame can equip a creature and grants its abilities")
    void createsCragflameAndEquipsCreature() {
        harness.castFromHand(player1, new MabelHeirToCragflame(), "{1}{R}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mabel = findPermanent(player1, "Mabel, Heir to Cragflame");
        Permanent cragflame = findPermanent(player1, "Cragflame");
        assertThat(cragflame.getCard().isToken()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int cragflameIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cragflame);
        harness.activateAbility(player1, cragflameIndex, 0, null, mabel.getId());
        harness.passBothPriorities();

        assertThat(cragflame.getAttachedTo()).isEqualTo(mabel.getId());
        assertThat(gqs.getEffectivePower(gd, mabel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mabel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mabel, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mabel, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mabel, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Mabel does not boost herself and her anthem ends when she leaves")
    void excludesSelfAndStopsBoostingWhenMabelLeaves() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        Permanent mabel = harness.addToBattlefieldAndReturn(player1, new MabelHeirToCragflame());

        assertThat(gqs.getEffectivePower(gd, mabel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mabel)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mouse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mouse)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mabel));

        assertThat(gqs.getEffectivePower(gd, mouse)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mouse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mabel boosts a creature with changeling as a Mouse")
    void boostsChangelingCreature() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.addToBattlefield(player1, new MabelHeirToCragflame());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cragflame can move to a non-Mouse and remains after Mabel leaves")
    void reequipsNonMouseAndRemainsWithoutMabel() {
        Permanent raccoon = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        harness.castFromHand(player1, new MabelHeirToCragflame(), "{1}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent mabel = findPermanent(player1, "Mabel, Heir to Cragflame");
        Permanent cragflame = findPermanent(player1, "Cragflame");
        assertThat(cragflame.getAttachedTo()).isNull();
        assertThat(countPermanents(player1, "Cragflame")).isEqualTo(1);

        int cragflameIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cragflame);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, cragflameIndex, 0, null, mabel.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, cragflameIndex, 0, null, raccoon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mabel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mabel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mabel, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mabel, Keyword.HASTE)).isFalse();
        assertThat(cragflame.getAttachedTo()).isEqualTo(raccoon.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mabel));

        harness.assertOnBattlefield(player1, "Cragflame");
        assertThat(gqs.getEffectivePower(gd, raccoon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raccoon)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, raccoon, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, raccoon, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, raccoon, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Mabel's enter trigger creates Cragflame even if Mabel leaves before resolution")
    void createsCragflameAfterMabelLeavesBeforeTriggerResolves() {
        harness.castFromHand(player1, new MabelHeirToCragflame(), "{1}{R}{W}");
        harness.passBothPriorities();
        Permanent mabel = findPermanent(player1, "Mabel, Heir to Cragflame");
        assertThat(countPermanents(player1, "Cragflame")).isZero();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mabel));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mabel, Heir to Cragflame");
        assertThat(countPermanents(player1, "Cragflame")).isEqualTo(1);
        Permanent cragflame = findPermanent(player1, "Cragflame");
        assertThat(cragflame.getCard().isToken()).isTrue();
        assertThat(cragflame.getAttachedTo()).isNull();
    }
}
