package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.m.MiasmicMummy;
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

@CardUsed({PitilessVizier.class, Censor.class, DuneBeetle.class, MiasmicMummy.class})
class PitilessVizierTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives this creature indestructible until end of turn")
    void cyclingGrantsIndestructible() {
        harness.addToBattlefield(player1, new PitilessVizier());
        // Cycling Censor discards it as a cost and triggers the grant.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the grant trigger

        assertThat(getPitilessVizier().getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new PitilessVizier());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(getPitilessVizier().getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(getPitilessVizier().getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cycling triggers exactly once and the grant resolves before the draw")
    void cyclingTriggersOnceBeforeDraw() {
        harness.addToBattlefield(player1, new PitilessVizier());
        harness.setHand(player1, List.of(new Censor()));
        DuneBeetle drawnCard = new DuneBeetle();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(getPitilessVizier().getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        harness.passBothPriorities();

        assertThat(getPitilessVizier().getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("An ordinary discard grants indestructible only to the discarding player's Vizier")
    void ordinaryDiscardGrantsIndestructible() {
        harness.addToBattlefield(player1, new PitilessVizier());
        Permanent opponentVizier = harness.addToBattlefieldAndReturn(player2, new PitilessVizier());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new MiasmicMummy(), "{1}{B}");
        harness.setHand(player1, List.of(new DuneBeetle()));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(getPitilessVizier().getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        resolveAllTriggers();

        assertThat(getPitilessVizier().getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(opponentVizier.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        harness.assertInGraveyard(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("An opponent cycling a card does not grant indestructible")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new PitilessVizier());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new DuneBeetle()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(getPitilessVizier().getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    private Permanent getPitilessVizier() {
        return findPermanent(player1, "Pitiless Vizier");
    }
}
