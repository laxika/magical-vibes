package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WoodlotCrawlerTest extends BaseCardTest {

    private Permanent attackWith(WoodlotCrawler card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Cannot be blocked when defending player controls a Forest")
    void forestwalkStopsBlock() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent attacker = attackWith(new WoodlotCrawler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Green creature cannot block it (protection from green)")
    void protectionStopsGreenBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent attacker = attackWith(new WoodlotCrawler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        Permanent crawler = new Permanent(new WoodlotCrawler());
        crawler.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crawler);

        // A legal target so the spell itself is playable.
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crawler.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by a black spell")
    void canBeTargetedByBlackSpell() {
        Permanent crawler = new Permanent(new WoodlotCrawler());
        crawler.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crawler);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, crawler.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }
}
