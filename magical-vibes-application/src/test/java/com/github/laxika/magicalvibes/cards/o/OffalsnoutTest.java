package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OffalsnoutTest extends BaseCardTest {

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB exiles a target card from an opponent's graveyard")
    void evokeExilesOpponentGraveyardCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new Offalsnout()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB (evoke sacrifice) -> LTB trigger -> graveyard prompt

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities(); // resolve LTB trigger -> exile

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getId().equals(bears.getId()))).isTrue();
        // Offalsnout itself was sacrificed as it entered.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Offalsnout"));
    }

    @Test
    @DisplayName("LTB fires on any leave and can exile a card from the controller's own graveyard")
    void leaveExilesOwnGraveyardCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent offalsnout = harness.addToBattlefieldAndReturn(player1, new Offalsnout());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, offalsnout);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> graveyard prompt

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities(); // resolve LTB trigger -> exile

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getId().equals(bears.getId()))).isTrue();
    }
}
