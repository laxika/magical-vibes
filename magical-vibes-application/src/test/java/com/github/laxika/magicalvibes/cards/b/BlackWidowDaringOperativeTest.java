package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackWidowDaringOperative.class, GrizzlyBears.class, LightningBolt.class})
class BlackWidowDaringOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Black Widow mills three cards when it enters")
    void millsThreeCardsOnEnter() {
        List<Card> milledCards = List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears());
        setDeck(player1, milledCards);
        harness.setHand(player1, List.of(new BlackWidowDaringOperative()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);
    }

    @Test
    @DisplayName("Combat damage makes each opponent lose life for each creature in the controller's graveyard")
    void combatDamageDrainsEachOpponentByCreatureGraveyardCount() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));

        Permanent blackWidow = addCreatureReady(player1, new BlackWidowDaringOperative());
        blackWidow.setAttacking(true);

        resolveCombat();

        // 3 combat damage plus 2 life loss for the two creature cards in the graveyard.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard are counted")
    void countsOnlyControllerCreatureCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent blackWidow = addCreatureReady(player1, new BlackWidowDaringOperative());
        blackWidow.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
