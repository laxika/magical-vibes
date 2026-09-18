package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToriDAvenantFuryRider.class, GoblinPiker.class, SavannahLions.class, GrizzlyBears.class})
class ToriDAvenantFuryRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts, grants trample to, and untaps the matching other attackers")
    void affectsMatchingOtherAttackers() {
        Permanent tori = addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent redAttacker = addCreatureReady(player1, new GoblinPiker());
        Permanent whiteAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent greenAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent redNonAttacker = addCreatureReady(player1, new GoblinPiker());
        Permanent opponentRed = addCreatureReady(player2, new GoblinPiker());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(tori.getPowerModifier()).isZero();
        assertThat(redAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(redAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, redAttacker, Keyword.TRAMPLE)).isTrue();
        assertThat(whiteAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(whiteAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(whiteAttacker.isTapped()).isFalse();
        assertThat(greenAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(greenAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(greenAttacker.isTapped()).isTrue();
        assertThat(redNonAttacker.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, redNonAttacker, Keyword.TRAMPLE)).isFalse();
        assertThat(opponentRed.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opponentRed, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and trample grant wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent redAttacker = addCreatureReady(player1, new GoblinPiker());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(redAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, redAttacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(redAttacker.getPowerModifier()).isZero();
        assertThat(redAttacker.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, redAttacker, Keyword.TRAMPLE)).isFalse();
    }
}
