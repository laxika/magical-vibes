package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Barishi.class, GrizzlyBears.class, HillGiant.class, Shock.class, WrathOfGod.class})
class BarishiTest extends BaseCardTest {

    /** Kills every creature on the battlefield so Barishi's death trigger resolves. */
    private void wrathAndResolveDeathTrigger() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves — Barishi dies and its trigger goes on the stack
        harness.passBothPriorities(); // the death trigger resolves
    }

    @Test
    @DisplayName("Dying Barishi is exiled and every creature card in its controller's graveyard is shuffled into the library")
    void diesExilesItselfAndShufflesCreatureCardsIntoLibrary() {
        harness.addToBattlefield(player1, new Barishi());
        Card barishiCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock, hillGiant));

        wrathAndResolveDeathTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(barishiCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId(), hillGiant.getId());
        // Only creature cards travel — Shock (and the Wrath that killed Barishi) stay in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(shock.getId())
                .doesNotContain(bears.getId(), hillGiant.getId(), barishiCard.getId());
    }

    @Test
    @DisplayName("Barishi never shuffles itself in — it is exiled before the graveyard is scanned")
    void doesNotShuffleItselfIn() {
        harness.addToBattlefield(player1, new Barishi());
        Card barishiCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        harness.setGraveyard(player1, List.of());

        wrathAndResolveDeathTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(barishiCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(barishiCard.getId());
    }

    @Test
    @DisplayName("A graveyard with no creature cards leaves everything in place")
    void noCreatureCardsLeavesGraveyardUntouched() {
        harness.addToBattlefield(player1, new Barishi());
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        wrathAndResolveDeathTrigger();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(shock.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(shock.getId());
    }
}
