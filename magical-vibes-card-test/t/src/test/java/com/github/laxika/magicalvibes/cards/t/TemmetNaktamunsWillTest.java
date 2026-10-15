package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemmetNaktamunsWill.class})
class TemmetNaktamunsWillTest extends BaseCardTest {

    @Test
    @DisplayName("Draws then discards once per attack and boosts your Zombies")
    void drawsDiscardsAndBoostsZombiesWhenYouAttack() {
        harness.addToBattlefield(player1, new TemmetNaktamunsWill());
        Permanent zombie = addCreatureReady(player1, creature("Zombie", 2, 2, CardSubtype.ZOMBIE));
        Permanent bear = addCreatureReady(player1, creature("Bear", 2, 2, CardSubtype.BEAR));
        Permanent opposingZombie = addCreatureReady(player2, creature("Opposing Zombie", 2, 2, CardSubtype.ZOMBIE));

        Card discarded = creature("Discarded card", 1, 1);
        Card drawn = creature("Drawn card", 1, 1);
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingZombie)).isEqualTo(2);
    }

    private static Card creature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
