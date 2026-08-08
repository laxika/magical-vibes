package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SireOfInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand at the end step")
    void bothPlayersDiscardHands() {
        addSire(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers at an opponent's end step too")
    void triggersOnOpponentEndStep() {
        addSire(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToEndStepAndResolve(player2);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addSire(Player player) {
        addPermanent(player, new SireOfInsanity());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
