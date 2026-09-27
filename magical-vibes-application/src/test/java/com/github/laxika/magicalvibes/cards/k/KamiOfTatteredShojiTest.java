package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.h.HundredTalonStrike;
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

@CardUsed({KamiOfTatteredShoji.class, KamiOfFalseHope.class, HundredTalonStrike.class, GoblinCohort.class})
class KamiOfTatteredShojiTest extends BaseCardTest {

    private Permanent addKami() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTatteredShoji());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return kami;
    }

    @Test
    @DisplayName("Gains flying when you cast a Spirit spell")
    void gainsFlyingOnSpiritCast() {
        Permanent kami = addKami();

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isFalse();

        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gains flying when you cast an Arcane spell")
    void gainsFlyingOnArcaneCast() {
        Permanent kami = addKami();

        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, kami.getId());

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger on a spell that is neither Spirit nor Arcane")
    void noTriggerOnUnrelatedSpell() {
        Permanent kami = addKami();

        harness.castFromHand(player1, new GoblinCohort(), "{R}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a Spirit spell")
    void noTriggerOnOpponentSpiritCast() {
        Permanent kami = addKami();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new KamiOfFalseHope(), "{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent kami = addKami();

        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, kami.getId());

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent afterCleanup = findPermanent(player1, "Kami of Tattered Shoji");
        assertThat(gqs.hasKeyword(gd, afterCleanup, Keyword.FLYING)).isFalse();
    }
}
