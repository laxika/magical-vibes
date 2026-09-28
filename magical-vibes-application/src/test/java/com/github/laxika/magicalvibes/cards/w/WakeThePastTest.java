package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({WakeThePast.class, ArcboundWorker.class, DarksteelRelic.class, GrizzlyBears.class})
class WakeThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all own artifact cards and gives returned creatures haste")
    void returnsOwnArtifactsWithHaste() {
        Card worker = new ArcboundWorker();
        Card relic = new DarksteelRelic();
        Card ownCreature = new GrizzlyBears();
        Card opponentArtifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(worker, relic, ownCreature));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(new WakeThePast()));
        addWakeThePastMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(worker, relic);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature)
                .anyMatch(card -> card instanceof WakeThePast);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
        Permanent returnedWorker = findPermanent(player1, "Arcbound Worker");
        assertThat(returnedWorker.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new ArcboundWorker()));
        harness.setHand(player1, List.of(new WakeThePast()));
        addWakeThePastMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent returnedWorker = findPermanent(player1, "Arcbound Worker");
        assertThat(returnedWorker.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(returnedWorker.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void addWakeThePastMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
