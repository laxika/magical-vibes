package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BrambleweftBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Brambleweft Behemoth deals excess combat damage to defending player via trample")
    void trampleDealsExcessDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent behemoth = addReady(player1);
        behemoth.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Brambleweft Behemoth is 6/6, blocker is 2/2 → assign lethal (2) to blocker, excess (4) to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReady(Player player) {
        Permanent perm = new Permanent(new BrambleweftBehemoth());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
