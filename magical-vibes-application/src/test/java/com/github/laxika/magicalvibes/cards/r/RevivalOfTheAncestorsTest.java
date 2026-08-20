package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RevivalOfTheAncestorsTest extends BaseCardTest {

    @Test
    void chapterICreatesThreeSpiritTokens() {
        harness.setHand(player1, List.of(new RevivalOfTheAncestors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        long spiritCount = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .count();
        assertThat(spiritCount).isEqualTo(3);
    }

    @Test
    void chapterIIDistributesCountersAmongOneOrMoreControlledCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RevivalOfTheAncestors());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIIGivesControlledCreaturesTrampleAndLifelink() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RevivalOfTheAncestors());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getGrantedKeywords())
                .contains(Keyword.TRAMPLE, Keyword.LIFELINK);
        assertThat(opponentCreature.getGrantedKeywords())
                .doesNotContain(Keyword.TRAMPLE, Keyword.LIFELINK);
    }
}
