package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.c.CarefulStudy;
import com.github.laxika.magicalvibes.cards.c.CoffinPurge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bearscape.class, AvenFisher.class, CarefulStudy.class, CoffinPurge.class})
class BearscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} and exiling two cards creates a green Bear token")
    void createsBearToken() {
        harness.addToBattlefield(player1, new Bearscape());
        harness.setGraveyard(player1, List.of(new AvenFisher(), new CarefulStudy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        Permanent bear = findPermanents(player1, "Bear").getFirst();
        assertThat(bear.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(bear.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(bear.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
        assertThat(bear.getCard().getPower()).isEqualTo(2);
        assertThat(bear.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without two cards in the graveyard")
    void requiresTwoCardsInGraveyard() {
        harness.addToBattlefield(player1, new Bearscape());
        harness.setGraveyard(player1, List.of(new AvenFisher()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Bear")).isEmpty();
    }

    @Test
    @DisplayName("The activation exiles exactly two cards and pays {1}{G}")
    void exilesExactlyTwoCardsAndPaysMana() {
        AvenFisher firstCard = new AvenFisher();
        CarefulStudy secondCard = new CarefulStudy();
        CoffinPurge thirdCard = new CoffinPurge();

        harness.addToBattlefield(player1, new Bearscape());
        harness.setGraveyard(player1, List.of(firstCard, secondCard, thirdCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstCard.getId(), thirdCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(firstCard, thirdCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
    }
}
