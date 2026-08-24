package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EbonyTreefolk.class)
class EbonyTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B}{G} gives Ebony Treefolk +1/+1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent treefolk = addReadyTreefolk(player1);
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ebony Treefolk's temporary boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent treefolk = addReadyTreefolk(player1);
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);
    }

    private Permanent addReadyTreefolk(Player player) {
        Permanent permanent = new Permanent(new EbonyTreefolk());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void addBlackGreenMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
