package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticSnake.class, GaeasSkyfolk.class, Index.class})
class MysticSnakeTest extends BaseCardTest {

    @Test
    @DisplayName("Flash ETB counters a target spell")
    void flashEtbCountersTargetSpell() {
        GaeasSkyfolk skyfolk = new GaeasSkyfolk();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, skyfolk, "{G}{U}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new MysticSnake(), "{1}{G}{U}{U}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(skyfolk.getId());

        harness.handlePermanentChosen(player1, skyfolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gaea's Skyfolk");
        harness.assertOnBattlefield(player1, "Mystic Snake");
    }

    @Test
    @DisplayName("ETB counters a noncreature spell")
    void etbCountersNoncreatureSpell() {
        Index index = new Index();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, index, "{U}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new MysticSnake(), "{1}{G}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(index.getId());

        harness.handlePermanentChosen(player1, index.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Index");
        harness.assertOnBattlefield(player1, "Mystic Snake");
    }

    @Test
    @DisplayName("ETB trigger is skipped when no spell is on the stack")
    void etbTriggerIsSkippedWithoutSpellTarget() {
        harness.castFromHand(player1, new MysticSnake(), "{1}{G}{U}{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Mystic Snake");
    }
}
