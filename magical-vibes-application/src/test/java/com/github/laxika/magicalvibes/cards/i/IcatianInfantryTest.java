package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IcatianInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants first strike until end of turn")
    void grantsFirstStrikeUntilEndOfTurn() {
        Permanent infantry = addInfantryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isFalse();

        expireTemporaryAbilities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The second ability grants banding until end of turn")
    void grantsBandingUntilEndOfTurn() {
        Permanent infantry = addInfantryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isFalse();

        expireTemporaryAbilities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isFalse();
    }

    private Permanent addInfantryReady(Player player) {
        Permanent perm = new Permanent(new IcatianInfantry());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void expireTemporaryAbilities() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
