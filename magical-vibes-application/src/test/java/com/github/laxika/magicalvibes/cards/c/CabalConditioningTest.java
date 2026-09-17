package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.cards.t.TwistedAbomination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalConditioning.class, CabalInterrogator.class, Stabilizer.class, TwistedAbomination.class})
class CabalConditioningTest extends BaseCardTest {

    @Test
    void eachTargetedPlayerDiscardsTheGreatestManaValueAmongControllerPermanents() {
        harness.addToBattlefield(player1, new CabalInterrogator());
        harness.addToBattlefield(player1, new TwistedAbomination());
        harness.setHand(player1, handWithConditioningAndInterrogators(6));
        harness.setHand(player2, interrogators(6));
        addBlackMana(7);

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        discardCards(player1, 6);
        discardCards(player2, 6);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    void evaluatesGreatestManaValueAtResolution() {
        harness.addToBattlefield(player1, new TwistedAbomination());
        harness.setHand(player1, List.of(new CabalConditioning()));
        harness.setHand(player2, interrogators(6));
        addBlackMana(7);

        harness.castSorcery(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new CabalInterrogator());
        harness.passBothPriorities();

        discardCards(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void countsNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Stabilizer());
        harness.setHand(player1, List.of(new CabalConditioning()));
        harness.setHand(player2, interrogators(2));
        addBlackMana(7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        discardCards(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void discardsNoCardsWhenControllerHasNoPermanents() {
        harness.setHand(player1, List.of(new CabalConditioning()));
        harness.setHand(player2, interrogators(2));
        addBlackMana(7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canBeCastWithNoTargets() {
        harness.setHand(player1, List.of(new CabalConditioning()));
        addBlackMana(7);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetAPermanent() {
        harness.addToBattlefield(player2, new CabalInterrogator());
        harness.setHand(player1, List.of(new CabalConditioning()));
        addBlackMana(7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Cabal Interrogator")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlackMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }

    private List<Card> interrogators(int amount) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            cards.add(new CabalInterrogator());
        }
        return cards;
    }

    private List<Card> handWithConditioningAndInterrogators(int interrogatorCount) {
        List<Card> cards = new ArrayList<>();
        cards.add(new CabalConditioning());
        cards.addAll(interrogators(interrogatorCount));
        return cards;
    }

    private void discardCards(Player player, int amount) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < amount; i++) {
            harness.handleCardChosen(player, 0);
        }
    }
}
