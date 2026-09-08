package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourageousGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the attack bonus without a creature with power 4 or greater")
    void doesNotBoostWithoutPowerFourCreature() {
        Permanent goblin = addCreatureReady(player1, new CourageousGoblin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not count a noncreature permanent with power 4 or greater")
    void doesNotBoostWithNoncreaturePowerFourPermanent() {
        Permanent goblin = addCreatureReady(player1, new CourageousGoblin());
        Card artifact = makeCreature("Large Artifact", 4, 4);
        artifact.setType(CardType.ARTIFACT);
        addCreatureReady(player1, artifact);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and menace when it attacks while controlling a creature with power 4 or greater")
    void boostsWithPowerFourCreature() {
        Permanent goblin = addCreatureReady(player1, new CourageousGoblin());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Keeps the bonus if the power 4 or greater creature leaves before resolution")
    void keepsBonusAfterConditionChanges() {
        Permanent goblin = addCreatureReady(player1, new CourageousGoblin());
        Permanent largeCreature = addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, largeCreature));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The attack bonus and menace wear off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent goblin = addCreatureReady(player1, new CourageousGoblin());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isFalse();
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
