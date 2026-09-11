package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NadaarSelflessPaladin.class, GrizzlyBears.class})
class NadaarSelflessPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        castNadaar(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    private void castNadaar(Player player) {
        harness.setHand(player, List.of(new NadaarSelflessPaladin()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0);
    }

    @Test
    @DisplayName("Attacking with Nadaar makes its controller venture into a dungeon")
    void attackingVentureIntoDungeon() {
        Permanent nadaar = harness.addToBattlefieldAndReturn(player1, new NadaarSelflessPaladin());
        nadaar.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("After completing a dungeon, other creatures you control get +1/+1")
    void buffsOtherCreaturesAfterDungeonCompletion() {
        Permanent nadaar = harness.addToBattlefieldAndReturn(player1, new NadaarSelflessPaladin());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int nadaarPowerBefore = gqs.getEffectivePower(gd, nadaar);
        int nadaarToughnessBefore = gqs.getEffectiveToughness(gd, nadaar);
        int bearsPowerBefore = gqs.getEffectivePower(gd, bears);
        int bearsToughnessBefore = gqs.getEffectiveToughness(gd, bears);

        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, nadaar)).isEqualTo(nadaarPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, nadaar)).isEqualTo(nadaarToughnessBefore);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughnessBefore + 1);
    }
}
