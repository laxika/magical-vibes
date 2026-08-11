package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeirOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a ferocious boost without a creature with power 4 or greater")
    void doesNotBoostWithoutFerocious() {
        Permanent heir = addCreatureReady(player1, new HeirOfTheWilds());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+1 when it attacks with ferocious")
    void boostsWithFerocious() {
        Permanent heir = addCreatureReady(player1, new HeirOfTheWilds());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);
        assertThat(heir.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ferocious boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent heir = addCreatureReady(player1, new HeirOfTheWilds());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
