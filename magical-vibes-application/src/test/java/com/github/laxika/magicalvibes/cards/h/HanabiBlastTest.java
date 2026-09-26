package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.k.KitsuneBlademaster;
import com.github.laxika.magicalvibes.cards.p.PerplexingChimera;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HanabiBlast.class, KitsuneBlademaster.class, WanderingOnes.class})
class HanabiBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void dealsTwoDamageToPlayer() {
        castHanabiBlast(List.of(new HanabiBlast()), player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature")
    void dealsTwoDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());

        castHanabiBlast(List.of(new HanabiBlast()), creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("With an empty hand the returned spell is the card discarded at random")
    void discardsItselfWithEmptyHand() {
        castHanabiBlast(List.of(new HanabiBlast()), player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Hanabi Blast");
    }

    @Test
    @DisplayName("With cards in hand exactly one card is discarded and the hand keeps its size")
    void discardsExactlyOneCardWithNonEmptyHand() {
        castHanabiBlast(List.of(new HanabiBlast(), new WanderingOnes(), new WanderingOnes()), player2.getId());

        // Either Hanabi Blast discarded itself (hand keeps both Wandering Ones) or it returned and one of
        // them was discarded (hand keeps Hanabi Blast + the other) — two cards either way, one in
        // the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(PerplexingChimera.class)
    @DisplayName("A controller other than the owner discards from the controller's hand")
    void controllerDiscardsFromTheirOwnHand() {
        harness.addToBattlefield(player2, new PerplexingChimera());
        harness.setHand(player1, List.of(new HanabiBlast()));
        harness.setHand(player2, List.of(new WanderingOnes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Hanabi Blast");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Wandering Ones");
    }

    private void castHanabiBlast(List<Card> hand, java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
