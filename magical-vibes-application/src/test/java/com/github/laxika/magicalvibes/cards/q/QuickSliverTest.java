package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.cards.w.WildPair;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuickSliver.class, MetallicSliver.class, AmoeboidChangeling.class, WildPair.class})
class QuickSliverTest extends BaseCardTest {

    @Test
    void controllerCanCastSliverAtInstantSpeed() {
        harness.addToBattlefield(player1, new QuickSliver());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new MetallicSliver(), "{1}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentCanCastSliverAtInstantSpeed() {
        harness.addToBattlefield(player1, new QuickSliver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new MetallicSliver(), "{1}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void nonSliverSpellDoesNotGetFlash() {
        harness.addToBattlefield(player1, new QuickSliver());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new WildPair(), "{4}{G}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void sliverLosesFlashWhenQuickSliverLeavesBattlefield() {
        harness.addToBattlefield(player1, new QuickSliver());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new MetallicSliver(), "{1}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void changelingSpellCountsAsSliverSpell() {
        harness.addToBattlefield(player1, new QuickSliver());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new AmoeboidChangeling(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
    }
}
