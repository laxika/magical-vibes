package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AndTheyShallKnowNoFear.class, GrizzlyBears.class, GoblinPiker.class})
class AndTheyShallKnowNoFearTest extends BaseCardTest {

    @Test
    void boostsAndGrantsIndestructibleToMatchingCreaturesYouControl() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AndTheyShallKnowNoFear()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownGoblin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void boostAndIndestructibleWearOffAtEndOfTurn() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AndTheyShallKnowNoFear()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void affectsEveryMatchingCreatureButNotCreaturesEnteringLater() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AndTheyShallKnowNoFear()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        Permanent laterBear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        for (Permanent bear : List.of(firstBear, secondBear)) {
            assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
        }
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canChooseACreatureTypeNotPresentOnTheBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AndTheyShallKnowNoFear()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "And They Shall Know No Fear");
    }
}
