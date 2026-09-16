package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NimbleMongoose;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HolisticWisdom.class, NimbleMongoose.class, Werebear.class, HowlingGale.class})
class HolisticWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a hand card and returns a graveyard card sharing a type")
    void returnsCardSharingTypeWithExiledCard() {
        Card target = new Werebear();
        harness.addToBattlefield(player1, new HolisticWisdom());
        harness.setHand(player1, List.of(new NimbleMongoose()));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Nimble Mongoose");
    }

    @Test
    @DisplayName("Keeps the target in the graveyard when it shares no type")
    void doesNothingForNonSharingTarget() {
        Card target = new HowlingGale();
        harness.addToBattlefield(player1, new HolisticWisdom());
        harness.setHand(player1, List.of(new NimbleMongoose()));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Nimble Mongoose");
    }

    @Test
    @DisplayName("Uses the selected hand card's type")
    void usesSelectedHandCardType() {
        Card unselectedCard = new NimbleMongoose();
        Card selectedCard = new HowlingGale();
        Card target = new HowlingGale();
        harness.addToBattlefield(player1, new HolisticWisdom());
        harness.setHand(player1, List.of(unselectedCard, selectedCard));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(unselectedCard, target)
                .doesNotContain(selectedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId()).contains(selectedCard.getId());
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card handCard = new NimbleMongoose();
        Card target = new Werebear();
        harness.addToBattlefield(player1, new HolisticWisdom());
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot activate without a card in hand to exile")
    void cannotActivateWithoutCardInHand() {
        Card target = new Werebear();
        harness.addToBattlefield(player1, new HolisticWisdom());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }
}
