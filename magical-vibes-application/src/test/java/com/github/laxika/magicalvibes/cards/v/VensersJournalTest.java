package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VensersJournalTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Venser's Journal has no maximum hand size static effect")
    void hasNoMaxHandSizeEffect() {
        VensersJournal card = new VensersJournal();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(NoMaximumHandSizeEffect.class);
    }

    @Test
    @DisplayName("Venser's Journal has upkeep life gain trigger")
    void hasUpkeepLifeGainTrigger() {
        VensersJournal card = new VensersJournal();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(GainLifePerCardsInHandEffect.class);
    }

    // ===== No maximum hand size =====

    @Test
    @DisplayName("Controller does not discard with more than 7 cards when Journal is on battlefield")
    void noMaxHandSizePreventsDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new VensersJournal());

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        // No discard prompt — Journal removes hand size limit
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    // ===== Upkeep life gain =====

    @Test
    @DisplayName("Gains life equal to number of cards in hand at upkeep")
    void gainsLifeEqualToHandSize() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain()
        )));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains no life when hand is empty")
    void gainsNoLifeWithEmptyHand() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life with exactly 1 card in hand")
    void gainsOneLifeWithOneCard() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain()
        )));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's Journal does not gain life for you")
    void opponentJournalDoesNotGainLifeForYou() {
        harness.addToBattlefield(player2, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain()
        )));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1's life unchanged — the Journal belongs to player2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life gain is logged")
    void lifeGainIsLogged() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest()
        )));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains 2 life"));
    }
}
