package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WojekHalberdiersTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants first strike to Wojek Halberdiers only")
    void battalionGrantsFirstStrike() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(otherAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike grant wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }
}
