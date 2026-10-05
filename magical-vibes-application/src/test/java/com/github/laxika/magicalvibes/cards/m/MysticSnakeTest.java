package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Darkness;
import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticSnake.class, FlyingMen.class, Darkness.class})
class MysticSnakeTest extends BaseCardTest {

    @Test
    @DisplayName("Flash ETB counters a target spell")
    void flashEtbCountersTargetSpell() {
        FlyingMen flyingMen = new FlyingMen();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, flyingMen, "{U}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new MysticSnake(), "{1}{G}{U}{U}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(flyingMen.getId());

        harness.handlePermanentChosen(player1, flyingMen.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Flying Men");
        harness.assertOnBattlefield(player1, "Mystic Snake");
    }

    @Test
    @DisplayName("ETB counters a noncreature spell")
    void etbCountersNoncreatureSpell() {
        Darkness darkness = new Darkness();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, darkness, "{B}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new MysticSnake(), "{1}{G}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(darkness.getId());

        harness.handlePermanentChosen(player1, darkness.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Darkness");
        harness.assertOnBattlefield(player1, "Mystic Snake");
    }

    @Test
    @DisplayName("ETB can counter its controller's own spell")
    void etbCountersControllersOwnSpell() {
        Darkness darkness = new Darkness();
        harness.castFromHand(player1, darkness, "{B}");
        harness.castFromHand(player1, new MysticSnake(), "{1}{G}{U}{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(darkness.getId());

        harness.handlePermanentChosen(player1, darkness.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Darkness");
        harness.assertOnBattlefield(player1, "Mystic Snake");
        assertThat(harness.getGameData().stack).isEmpty();
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
