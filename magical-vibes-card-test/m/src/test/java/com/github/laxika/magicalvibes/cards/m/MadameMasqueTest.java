package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadameMasque.class, Forest.class})
class MadameMasqueTest extends BaseCardTest {

    @Test
    @DisplayName("Connives when it enters and gets a counter for discarding a nonland card")
    void connivesOnEnter() {
        Card drawnCard = new Forest();
        Card discardedCard = new MadameMasque();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, new ArrayList<>(List.of(new MadameMasque(), discardedCard)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent masque = findPermanent(player1, "Madame Masque");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(masque.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("Creates a Villain token when its controller draws their second card each turn")
    void createsVillainOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new MadameMasque());
        harness.setLibrary(player1, List.of(new MadameMasque(), new MadameMasque(), new MadameMasque()));

        draw(player1);
        draw(player1);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
        assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardingLandDoesNotAddCounter() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new MadameMasque()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Madame Masque").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
    }

    @Test
    void conniveDrawCanBeSecondDrawAndCreateToken() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new MadameMasque()));
        draw(player1);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentDrawingSecondCardDoesNotTrigger() {
        harness.addToBattlefield(player1, new MadameMasque());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void conniveUsesCurrentControllerWhenControlChangesBeforeResolution() {
        Card drawnCard = new Forest();
        Card discardedCard = new MadameMasque();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player1, List.of(new MadameMasque()));
        harness.setHand(player2, List.of(discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent masque = findPermanent(player1, "Madame Masque");
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(masque);
            gd.playerBattlefields.get(player2.getId()).add(masque);
        });
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(discardedCard, drawnCard);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(masque.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

}
