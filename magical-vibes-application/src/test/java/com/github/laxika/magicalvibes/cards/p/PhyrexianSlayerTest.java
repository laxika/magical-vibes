package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked by a white creature destroys that creature")
    void destroysWhiteBlocker() {
        Permanent slayer = addReadySlayer();
        slayer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setRegenerationShield(1);

        block(blocker);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Becoming blocked by a nonwhite creature does not destroy it")
    void doesNotDestroyNonwhiteBlocker() {
        Permanent slayer = addReadySlayer();
        slayer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.getGrantedKeywords().add(Keyword.FLYING);

        block(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("With multiple blockers, only white blockers are destroyed")
    void onlyWhiteBlockersAreDestroyed() {
        Permanent slayer = addReadySlayer();
        slayer.setAttacking(true);
        Permanent whiteBlocker = addCreatureReady(player2, new SerraAngel());
        Permanent nonwhiteBlocker = addCreatureReady(player2, new HillGiant());
        nonwhiteBlocker.getGrantedKeywords().add(Keyword.FLYING);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(whiteBlocker.getId()))
                .anyMatch(permanent -> permanent.getId().equals(nonwhiteBlocker.getId()));
    }

    private Permanent addReadySlayer() {
        return addCreatureReady(player1, new PhyrexianSlayer());
    }

    private void block(Permanent blocker) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        harness.passBothPriorities();
    }
}
