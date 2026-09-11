package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Wargling.class)
class WarglingTest extends BaseCardTest {

    @Test
    @DisplayName("Ferocious attack gives Wargling +1/+0 and your creatures trample")
    void ferociousAttackBoostsAndGrantsTrample() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        Permanent ally = addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wargling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Without a creature with power 4 or greater, ferocious does not trigger")
    void doesNotTriggerWithoutLargeCreature() {
        Permanent wargling = addCreatureReady(player1, new Wargling());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Ferocious bonuses wear off at end of turn")
    void bonusesWearOffAtEndOfTurn() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        Permanent ally = addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isFalse();
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
