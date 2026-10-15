package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.Eliminate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasStandardBearer.class, Forest.class, Eliminate.class})
class LilianasStandardBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each creature that died under its controller's control this turn")
    void drawsForControllerCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LilianasStandardBearer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
    }

    @Test
    @DisplayName("Does not draw for creatures that died under an opponent's control")
    void onlyCountsControllerCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LilianasStandardBearer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Counts its own death while its enter trigger is on the stack")
    void countsItselfWhenItDiesBeforeTriggerResolves() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard, new Forest()));
        harness.setHand(player1, List.of(new LilianasStandardBearer()));
        harness.setHand(player2, List.of(new Eliminate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Liliana's Standard Bearer"));
        harness.assertInGraveyard(player1, "Liliana's Standard Bearer");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can be cast on the opponent's turn and draws nothing with no deaths")
    void flashesInOnOpponentsTurnWithNoDeaths() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new LilianasStandardBearer()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Liliana's Standard Bearer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
