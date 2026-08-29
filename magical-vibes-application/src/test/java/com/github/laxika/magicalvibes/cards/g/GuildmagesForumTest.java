package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuildmagesForumTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds a colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new GuildmagesForum());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability gives a multicolored creature an additional counter")
    void multicoloredCreatureEntersWithAdditionalCounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        Card creature = multicoloredCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(creature));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, creature.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter rider does not apply to a monocolored creature")
    void monocoloredCreatureDoesNotGetAdditionalCounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Card multicoloredCreature() {
        Card card = new Card();
        card.setName("Test Red-Blue Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{R}");
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED, CardColor.BLUE));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
