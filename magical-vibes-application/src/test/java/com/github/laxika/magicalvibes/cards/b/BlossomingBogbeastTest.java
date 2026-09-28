package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BlossomingBogbeast.class, GrizzlyBears.class})
class BlossomingBogbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life and boosts all own creatures with trample by life gained this turn")
    void gainsLifeAndBoostsOwnCreatures() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        attackWith(bogbeast);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bogbeast)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bogbeast, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack boost and trample wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());

        attackWith(bogbeast);

        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bogbeast, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bogbeast, Keyword.TRAMPLE)).isFalse();
    }

    private void attackWith(Permanent creature) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();
    }
}
