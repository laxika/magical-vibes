package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.r.RouseTheMob;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkroanLineBreaker.class, GiantGrowth.class, Shock.class, RouseTheMob.class})
class AkroanLineBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Akroan Line Breaker gives it +2/+0 and intimidate")
    void castingSpellThatTargetsLineBreakerTriggersHeroic() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, lineBreaker.getId());

        assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Akroan Line Breaker's Heroic bonuses wear off at the end of the turn")
    void heroicBonusesWearOffAtEndOfTurn() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, lineBreaker.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Akroan Line Breaker's Heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell that targets Akroan Line Breaker does not trigger its Heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID lineBreakerId = lineBreaker.getId();
        harness.castAndResolveInstant(player2, 0, lineBreakerId);

        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void successiveTargetedSpellsEachAddAHeroicBonus() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new RouseTheMob(), new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, lineBreaker.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, lineBreaker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, lineBreaker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isTrue();

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(10);
    }

    @Test
    void multiTargetSpellTriggersEachTargetedLineBreakerOnce() {
        Permanent first = addCreatureReady(player1, new AkroanLineBreaker());
        Permanent second = addCreatureReady(player1, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        for (Permanent lineBreaker : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, lineBreaker)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isTrue();
        }
    }

    @Test
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        Permanent other = addCreatureReady(player2, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lineBreaker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.INTIMIDATE)).isFalse();
    }
}
