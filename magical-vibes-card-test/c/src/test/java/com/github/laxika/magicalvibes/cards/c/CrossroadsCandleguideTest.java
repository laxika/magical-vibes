package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrossroadsCandleguide.class})
class CrossroadsCandleguideTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one chosen card from a graveyard")
    void etbExilesChosenGraveyardCard() {
        Card graveyardCard = new CrossroadsCandleguide();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        castCrossroadsCandleguide();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Crossroads Candleguide");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Crossroads Candleguide"));
    }

    @Test
    @DisplayName("ETB may choose zero cards")
    void etbMayChooseZeroCards() {
        Card graveyardCard = new CrossroadsCandleguide();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        castCrossroadsCandleguide();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crossroads Candleguide");
    }

    @Test
    @DisplayName("Pays {2}, adds one chosen color of mana, and does not tap")
    void manaAbilityAddsChosenColorWithoutTapping() {
        Permanent candleguide = harness.addToBattlefieldAndReturn(player1, new CrossroadsCandleguide());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(candleguide.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana ability requires two mana")
    void manaAbilityRequiresTwoMana() {
        harness.addToBattlefield(player1, new CrossroadsCandleguide());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void etbCanExileFromOwnGraveyard() {
        Card target = new CrossroadsCandleguide();
        harness.setGraveyard(player1, List.of(target));
        castCrossroadsCandleguide();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Crossroads Candleguide");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void etbWithEmptyGraveyardsResolvesWithoutChoosingTargets() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        castCrossroadsCandleguide();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void etbDoesNotExileAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new CrossroadsCandleguide();
        Card other = new CrossroadsCandleguide();
        harness.setGraveyard(player2, List.of(target, other));
        castCrossroadsCandleguide();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void manaAbilityCanBeRepeatedWhileTappedAndDoesNotUseStack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrossroadsCandleguide());
        source.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color.name());
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void castCrossroadsCandleguide() {
        harness.castFromHand(player1, new CrossroadsCandleguide(), "{4}");
    }
}
