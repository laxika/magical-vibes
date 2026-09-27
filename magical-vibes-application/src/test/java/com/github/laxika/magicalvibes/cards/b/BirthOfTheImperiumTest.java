package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BirthOfTheImperium.class, GrizzlyBears.class})
class BirthOfTheImperiumTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates one vigilant Astartes Warrior per opponent")
    void chapterICreatesAstartesWarriors() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
            assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter II makes each opponent sacrifice a creature")
    void chapterIIMakesOpponentSacrificeCreature() {
        harness.addToBattlefield(player1, new BirthOfTheImperium());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Birth of the Imperium"))
                .findFirst().orElseThrow();
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chapter III draws two cards when you control more creatures")
    void chapterIIIDrawsWhenYouControlMoreCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter III does not draw when creature counts are tied")
    void chapterIIIDoesNotDrawWhenCreatureCountsAreTied() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
