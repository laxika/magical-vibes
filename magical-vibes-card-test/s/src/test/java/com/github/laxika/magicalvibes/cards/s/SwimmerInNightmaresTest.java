package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwimmerInNightmares.class, GrizzlyBears.class, Spellbook.class})
class SwimmerInNightmaresTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 when a single graveyard has ten cards")
    void getsPowerBoostWhenSingleGraveyardHasTenCards() {
        addSwimmer();
        fillGraveyard(player2, 10);

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Does not get +3/+0 when no single graveyard has ten cards")
    void doesNotGetPowerBoostWhenCardsAreSplitBetweenGraveyards() {
        addSwimmer();
        fillGraveyard(player1, 5);
        fillGraveyard(player2, 5);

        assertStats(1, 4);
    }

    @Test
    @DisplayName("Gets +3/+0 from its controller's graveyard")
    void ownGraveyardCounts() {
        addSwimmer();
        fillGraveyard(player1, 10);

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Cannot be blocked while its controller controls an Ashiok planeswalker")
    void cannotBeBlockedWithAshiokPlaneswalker() {
        addSwimmer();
        harness.addToBattlefield(player1, createAshiokPlaneswalker());

        Permanent swimmer = findPermanent(player1, "Swimmer in Nightmares");
        assertThat(gqs.hasCantBeBlocked(gd, swimmer)).isTrue();
    }

    @Test
    @DisplayName("Can be blocked without a qualifying Ashiok planeswalker")
    void canBeBlockedWithoutAshiokPlaneswalker() {
        addSwimmer();
        harness.addToBattlefield(player1, createJacePlaneswalker());
        harness.addToBattlefield(player2, createAshiokPlaneswalker());

        Permanent swimmer = findPermanent(player1, "Swimmer in Nightmares");
        assertThat(gqs.hasCantBeBlocked(gd, swimmer)).isFalse();
    }

    private void addSwimmer() {
        harness.addToBattlefield(player1, new SwimmerInNightmares());
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private Card createAshiokPlaneswalker() {
        Card card = createPlaneswalker();
        card.setSubtypes(List.of(CardSubtype.ASHIOK));
        return card;
    }

    private Card createJacePlaneswalker() {
        Card card = createPlaneswalker();
        card.setSubtypes(List.of(CardSubtype.JACE));
        return card;
    }

    private Card createPlaneswalker() {
        Card card = new GrizzlyBears();
        card.setType(CardType.PLANESWALKER);
        return card;
    }

    private void assertStats(int power, int toughness) {
        Permanent swimmer = findPermanent(player1, "Swimmer in Nightmares");
        assertThat(gqs.getEffectivePower(gd, swimmer)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, swimmer)).isEqualTo(toughness);
    }
}
