package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoliathSpider.class, CourierHawk.class})
class GoliathSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Goliath Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new CourierHawk());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
