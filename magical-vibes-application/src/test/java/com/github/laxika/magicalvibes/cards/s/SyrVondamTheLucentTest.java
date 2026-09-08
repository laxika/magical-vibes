package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SyrVondamTheLucent.class, GrizzlyBears.class})
class SyrVondamTheLucentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other creatures you control and gives them deathtouch")
    void entersAndBoostsOtherControlledCreatures() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Attacking boosts other creatures you control and gives them deathtouch")
    void attacksAndBoostsOtherControlledCreatures() {
        Permanent source = addCreatureReady(player1, new SyrVondamTheLucent());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and deathtouch grant wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
    }
}
