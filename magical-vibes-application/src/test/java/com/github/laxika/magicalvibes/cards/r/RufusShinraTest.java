package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RufusShinra.class)
class RufusShinraTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a legendary 2/2 white and black Dog token")
    void attackingCreatesDarkstar() {
        addCreatureReady(player1, new RufusShinra());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent darkstar = findPermanent(player1, "Darkstar");
        assertThat(darkstar.getCard().getPower()).isEqualTo(2);
        assertThat(darkstar.getCard().getToughness()).isEqualTo(2);
        assertThat(darkstar.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(darkstar.getCard().getSubtypes()).contains(CardSubtype.DOG);
        assertThat(darkstar.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Does not create Darkstar when its controller already controls one")
    void doesNotCreateDarkstarWhenAlreadyControlled() {
        addCreatureReady(player1, new RufusShinra());
        harness.addToBattlefield(player1, darkstarCard());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Darkstar")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create Darkstar if one appears before the trigger resolves")
    void rechecksDarkstarConditionOnResolution() {
        addCreatureReady(player1, new RufusShinra());

        declareAttackers(List.of(0));
        harness.addToBattlefield(player1, darkstarCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Darkstar")).isEqualTo(1);
    }

    private Card darkstarCard() {
        Card card = new Card();
        card.setName("Darkstar");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
        return card;
    }
}
