package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrneryDilophosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a boost without a creature with power 4 or greater")
    void doesNotBoostWithoutLargeCreature() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(dilophosaur.getPowerModifier()).isEqualTo(0);
        assertThat(dilophosaur.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+2 when it attacks while its controller has a creature with power 4 or greater")
    void boostsWithLargeCreature() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(dilophosaur.getPowerModifier()).isEqualTo(2);
        assertThat(dilophosaur.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dilophosaur.getPowerModifier()).isEqualTo(0);
        assertThat(dilophosaur.getToughnessModifier()).isEqualTo(0);
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
