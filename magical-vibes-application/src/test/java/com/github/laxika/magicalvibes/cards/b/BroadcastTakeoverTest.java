package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroadcastTakeover.class, DarksteelIngot.class})
class BroadcastTakeoverTest extends BaseCardTest {

    @Test
    @DisplayName("Steals opponent artifacts, untaps them and gives them haste")
    void stealsUntapsAndHastesOpponentArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        ownArtifact.tap();
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        opponentArtifact.tap();

        castBroadcastTakeover();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact, opponentArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.HASTE)).isFalse();
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Returns the stolen artifacts and removes haste at end of turn")
    void returnsStolenArtifactsAtEndOfTurn() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        castBroadcastTakeover();

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentArtifact);
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HASTE)).isFalse();
    }

    private void castBroadcastTakeover() {
        harness.setHand(player1, List.of(new BroadcastTakeover()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
