package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
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

@CardUsed({CabalConditioning.class, GrizzlyBears.class, WurmcoilEngine.class})
class CabalConditioningTest extends BaseCardTest {

    @Test
    void eachTargetedPlayerDiscardsTheGreatestManaValueAmongControllerPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WurmcoilEngine());
        harness.setHand(player1, handWithConditioningAndBears(6));
        harness.setHand(player2, bears(6));
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
        harness.addToBattlefield(player1, new WurmcoilEngine());
        harness.setHand(player1, List.of(new CabalConditioning()));
        harness.setHand(player2, bears(6));
        addBlackMana(7);

        harness.castSorcery(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        discardCards(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CabalConditioning()));
        addBlackMana(7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlackMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }

    private List<Card> bears(int amount) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private List<Card> handWithConditioningAndBears(int bearCount) {
        List<Card> cards = new ArrayList<>();
        cards.add(new CabalConditioning());
        cards.addAll(bears(bearCount));
        return cards;
    }

    private void discardCards(Player player, int amount) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < amount; i++) {
            harness.handleCardChosen(player, 0);
        }
    }
}
