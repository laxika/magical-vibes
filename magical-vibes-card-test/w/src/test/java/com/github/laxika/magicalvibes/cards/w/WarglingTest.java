package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
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

@CardUsed({Wargling.class, OrdinaryBear.class})
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

    @Test
    @DisplayName("Ferocious does not recheck power after the qualifying creature leaves")
    void bonusResolvesAfterQualifyingCreatureLeaves() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        Permanent bear = addCreatureReady(player1, new OrdinaryBear());
        Permanent ally = addCreatureReady(player1, new Wargling());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's large creature cannot satisfy ferocious")
    void opposingCreatureDoesNotEnableFerocious() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        addCreatureReady(player2, new OrdinaryBear());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Removing Wargling does not prevent its allies gaining trample")
    void alliesGainTrampleAfterSourceLeaves() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        Permanent bear = addCreatureReady(player1, new OrdinaryBear());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wargling));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trample applies to creatures present at resolution, but not later arrivals or opponents")
    void trampleRecipientsAreDeterminedAtResolution() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        addCreatureReady(player1, new OrdinaryBear());
        Permanent opponent = addCreatureReady(player2, new OrdinaryBear());

        declareAttackers(player1, List.of(0));
        Permanent beforeResolution = addCreatureReady(player1, new Wargling());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new Wargling());

        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wargling itself can satisfy ferocious using its effective power")
    void sourceCanSatisfyFerocious() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        wargling.setPowerModifier(2);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Power three is insufficient and a later large creature cannot create the trigger")
    void ferociousMustBeSatisfiedWhenAttacking() {
        Permanent wargling = addCreatureReady(player1, new Wargling());
        wargling.setPowerModifier(1);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isEmpty();
        addCreatureReady(player1, new OrdinaryBear());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wargling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wargling, Keyword.TRAMPLE)).isFalse();
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
