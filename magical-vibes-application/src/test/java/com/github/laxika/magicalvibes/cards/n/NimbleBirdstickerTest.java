package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.o.OrchardSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NimbleBirdstickerTest extends BaseCardTest {

    @Test
    @DisplayName("Nimble Birdsticker can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = new Permanent(new OrchardSpirit());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent birdsticker = new Permanent(new NimbleBirdsticker());
        birdsticker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(birdsticker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(birdsticker.isBlocking()).isTrue();
    }
}
