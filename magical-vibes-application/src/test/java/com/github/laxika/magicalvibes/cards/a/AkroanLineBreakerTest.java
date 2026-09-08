package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AkroanLineBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Akroan Line Breaker gives it +2/+0 and intimidate")
    void castingSpellThatTargetsLineBreakerTriggersHeroic() {
        Permanent lineBreaker = addCreatureReady(player1, new AkroanLineBreaker());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, lineBreaker.getId());
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, lineBreakerId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lineBreaker, Keyword.INTIMIDATE)).isFalse();
    }
}
