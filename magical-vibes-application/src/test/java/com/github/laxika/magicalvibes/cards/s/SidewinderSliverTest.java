package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidewinderSliver.class, BonesplitterSliver.class, AshcoatBear.class})
class SidewinderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Sidewinder Sliver grants itself flanking")
    void grantsSelfFlanking() {
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Grants flanking to another Sliver you control")
    void grantsFlankingToOtherSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Grants flanking to an opponent's Sliver too")
    void grantsFlankingToOpponentSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flanking to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Granted flanking gives a non-flanking blocker -1/-1")
    void grantedFlankingShrinksNonFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new SidewinderSliver());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }
}
