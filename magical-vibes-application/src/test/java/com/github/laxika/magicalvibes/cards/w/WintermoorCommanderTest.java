package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheKeep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WintermoorCommander.class, KnightOfTheKeep.class, GrizzlyBears.class})
class WintermoorCommanderTest extends BaseCardTest {

    @Test
    void toughnessEqualsKnightsYouControl() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        addCreatureReady(player1, new KnightOfTheKeep());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new KnightOfTheKeep());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);

        addCreatureReady(player1, new KnightOfTheKeep());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(3);
    }

    @Test
    void attackingGivesAnotherKnightYouControlIndestructibleUntilEndOfTurn() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        Permanent knight = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent nonKnight = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKnight = addCreatureReady(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonKnight, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.INDESTRUCTIBLE)).isFalse();

        advanceToEndStep(player1);

        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
