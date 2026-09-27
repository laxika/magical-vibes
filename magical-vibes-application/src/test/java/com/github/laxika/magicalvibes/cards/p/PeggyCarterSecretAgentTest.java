package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeggyCarterSecretAgent.class, GrizzlyBears.class})
class PeggyCarterSecretAgentTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking alone gains indestructible until end of turn")
    void attackingAloneGainsIndestructible() {
        addCreatureReady(player1, new PeggyCarterSecretAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Peggy Carter gains indestructible when she attacks alone")
    void selfAttackingAloneGainsIndestructible() {
        Permanent peggy = addCreatureReady(player1, new PeggyCarterSecretAgent());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(peggy.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PeggyCarterSecretAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when multiple creatures attack")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new PeggyCarterSecretAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Peggy Carter, Secret Agent"));
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
