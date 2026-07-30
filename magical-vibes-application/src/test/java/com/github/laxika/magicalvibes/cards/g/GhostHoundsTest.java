package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhostHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a white creature gives Ghost Hounds first strike")
    void blocksWhiteCreatureGrantsFirstStrike() {
        Permanent attacker = addReady(player1, new EliteVanguard());
        attacker.setAttacking(true);
        Permanent hounds = addReady(player2, new GhostHounds());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hounds.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(hounds.getPowerModifier()).isZero();
        assertThat(hounds.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking a non-white creature gives Ghost Hounds nothing")
    void blocksNonWhiteCreatureDoesNothing() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent hounds = addReady(player2, new GhostHounds());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hounds.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Becoming blocked by a white creature gives Ghost Hounds first strike")
    void becomesBlockedByWhiteCreatureGrantsFirstStrike() {
        Permanent hounds = addReady(player1, new GhostHounds());
        hounds.setAttacking(true);
        addReady(player2, new EliteVanguard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hounds.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Becoming blocked by a non-white creature gives Ghost Hounds nothing")
    void becomesBlockedByNonWhiteCreatureDoesNothing() {
        Permanent hounds = addReady(player1, new GhostHounds());
        hounds.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hounds.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
