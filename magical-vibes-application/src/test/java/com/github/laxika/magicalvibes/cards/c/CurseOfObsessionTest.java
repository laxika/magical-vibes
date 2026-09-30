package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfObsession.class, Python.class})
class CurseOfObsessionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted player draws two additional cards during their draw step")
    void enchantedPlayerDrawsTwoAdditionalCards() {
        attachCurseTo(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new Python())));
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Draw trigger does not fire during another player's draw step")
    void drawTriggerOnlyFiresForEnchantedPlayer() {
        attachCurseTo(player2);
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore);
    }

    @Test
    @DisplayName("Enchanted player discards their hand at their end step")
    void enchantedPlayerDiscardsHandAtEndStep() {
        attachCurseTo(player2);
        harness.setHand(player2, new ArrayList<>(List.of(
                new Python(), new Python(), new Python())));
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 3);
    }

    @Test
    @DisplayName("End-step discard does not fire during another player's end step")
    void discardOnlyFiresForEnchantedPlayerEndStep() {
        attachCurseTo(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new Python(), new Python())));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToEndStep(player1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);

        harness.passBothPriorities();
    }

    private Permanent attachCurseTo(Player enchantedPlayer) {
        Permanent curse = new Permanent(new CurseOfObsession());
        curse.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
        return curse;
    }

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
