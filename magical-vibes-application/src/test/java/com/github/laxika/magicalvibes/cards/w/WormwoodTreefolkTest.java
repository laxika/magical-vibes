package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WormwoodTreefolk.class)
class WormwoodTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("The green ability grants forestwalk and deals 2 damage to its controller")
    void greenAbilityGrantsForestwalkAndDealsDamage() {
        Permanent treefolk = addReadyTreefolk();
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.FORESTWALK)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The black ability grants swampwalk and deals 2 damage to its controller")
    void blackAbilityGrantsSwampwalkAndDealsDamage() {
        Permanent treefolk = addReadyTreefolk();
        harness.addMana(player1, ManaColor.BLACK, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.SWAMPWALK)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The granted landwalk ability wears off at end of turn")
    void grantedLandwalkWearsOffAtEndOfTurn() {
        Permanent treefolk = addReadyTreefolk();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.FORESTWALK)).isFalse();
    }

    private Permanent addReadyTreefolk() {
        Permanent treefolk = new Permanent(new WormwoodTreefolk());
        treefolk.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(treefolk);
        return treefolk;
    }
}
