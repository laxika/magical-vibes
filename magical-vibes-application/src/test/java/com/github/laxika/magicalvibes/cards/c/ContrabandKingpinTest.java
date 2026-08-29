package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContrabandKingpinTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering causes you to scry 1")
    void allyArtifactEntryCausesScry() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not cause a scry")
    void opponentArtifactEntryDoesNotCauseScry() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
