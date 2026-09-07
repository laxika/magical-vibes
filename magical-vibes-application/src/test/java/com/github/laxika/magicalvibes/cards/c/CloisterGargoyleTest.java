package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CloisterGargoyle.class)
class CloisterGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        castCloisterGargoyle(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playersWhoCompletedDungeon).doesNotContain(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Gets +3/+0 and flying after its controller completes a dungeon")
    void getsBonusAfterDungeonCompletion() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new CloisterGargoyle());
        int powerBefore = gqs.getEffectivePower(gd, gargoyle);
        int toughnessBefore = gqs.getEffectiveToughness(gd, gargoyle);

        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
    }

    private void castCloisterGargoyle(Player player) {
        harness.setHand(player, List.of(new CloisterGargoyle()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0);
    }
}
