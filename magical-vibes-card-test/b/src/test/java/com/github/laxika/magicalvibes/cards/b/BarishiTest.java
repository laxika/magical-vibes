package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PhyrexianFurnace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Barishi.class, GrizzlyBears.class, HillGiant.class, Shock.class, WrathOfGod.class,
        PhyrexianFurnace.class})
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

    @Test
    @DisplayName("Removing Barishi in response does not prevent shuffling the remaining creature cards")
    void shufflesCreaturesWhenBarishiIsExiledInResponse() {
        Card barishi = new Barishi();
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, barishi);
        harness.addToBattlefield(player2, new PhyrexianFurnace());
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player2, List.of(new Shock()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 1, null, barishi.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(barishi);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(bears).doesNotContain(barishi);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Simultaneously dying creatures are shuffled only from Barishi's controller's graveyard")
    void shufflesSimultaneouslyDyingFriendlyCreaturesOnly() {
        Card bears = new GrizzlyBears();
        Card opponentGiant = new HillGiant();
        harness.addToBattlefield(player1, new Barishi());
        harness.addToBattlefield(player1, bears);
        harness.addToBattlefield(player2, opponentGiant);

        wrathAndResolveDeathTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).contains(bears).doesNotContain(opponentGiant);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentGiant);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(opponentGiant);
    }
}
