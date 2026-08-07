package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MistmoonGriffinTest extends BaseCardTest {

    /** Kills every creature on the battlefield so the Griffin's death trigger resolves. */
    private void wrathAndResolveDeathTrigger() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — the Griffin dies and its trigger goes on the stack
        harness.passBothPriorities(); // the death trigger resolves
    }

    @Test
    @DisplayName("Dying Griffin is exiled and the top creature card of its controller's graveyard is reanimated")
    void diesExilesItselfAndReanimatesTopCreatureCard() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card griffinCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        // Hill Giant is the last card put into the graveyard, so it is the top creature card.
        harness.setGraveyard(player1, List.of(bears, hillGiant));

        wrathAndResolveDeathTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(griffinCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(hillGiant.getId());
        // Grizzly Bears stays behind — only one creature card comes back. (Wrath of God itself is
        // also in the graveyard by now, so this is a contains-check, not an exact match.)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId())
                .doesNotContain(hillGiant.getId());
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped")
    void skipsNoncreatureCardsAboveTheTopCreatureCard() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));

        wrathAndResolveDeathTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(shock.getId())
                .doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("The Griffin never reanimates itself — it is exiled before the top creature card is looked up")
    void doesNotReanimateItself() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card griffinCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        harness.setGraveyard(player1, List.of());

        wrathAndResolveDeathTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(griffinCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
