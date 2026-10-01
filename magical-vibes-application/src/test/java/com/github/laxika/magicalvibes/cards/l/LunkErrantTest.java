package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LunkErrant.class, ElvishWarrior.class})
class LunkErrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone — gets +1/+1 and gains trample until end of turn")
    void attackingAloneBoostsAndGrantsTrample() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with another creature — no boost, no trample")
    void attackingWithOtherCreatureNoEffect() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());
        addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking alone â€” boost and trample expire at end of turn")
    void attackingAloneEffectsExpireAtEndOfTurn() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isFalse();
    }
}
