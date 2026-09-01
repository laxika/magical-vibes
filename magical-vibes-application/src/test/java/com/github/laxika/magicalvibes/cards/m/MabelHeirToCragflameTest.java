package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MabelHeirToCragflame.class, ManifoldMouse.class, GrizzlyBears.class})
class MabelHeirToCragflameTest extends BaseCardTest {

    @Test
    @DisplayName("Other Mice you control get +1/+1")
    void buffsOtherMiceYouControl() {
        Permanent ownMouse = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        Permanent nonMouse = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        harness.setHand(player1, List.of(new MabelHeirToCragflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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
}
