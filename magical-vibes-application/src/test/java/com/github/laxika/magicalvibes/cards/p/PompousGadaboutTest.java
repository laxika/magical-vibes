package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PompousGadabout.class, GrizzlyBears.class})
class PompousGadaboutTest extends BaseCardTest {

    @Test
    @DisplayName("Pompous Gadabout has hexproof only during its controller's turn")
    void hexproofOnlyDuringControllerTurn() {
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, gadabout, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, gadabout, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Pompous Gadabout cannot be blocked by a face-down creature")
    void cannotBeBlockedByFaceDownCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        gadabout.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gadabout);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pompous Gadabout can be blocked by a creature with a name")
    void canBeBlockedByNamedCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        gadabout.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gadabout);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
