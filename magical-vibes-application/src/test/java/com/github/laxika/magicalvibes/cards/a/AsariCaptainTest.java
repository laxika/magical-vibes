package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AsariCaptain.class, MothriderSamurai.class, ElvishWarrior.class, GrizzlyBears.class})
class AsariCaptainTest extends BaseCardTest {

    @Test
    void samuraiAttackingAloneGetsPowerForEachSamuraiOrWarrior() {
        Permanent asariCaptain = addCreatureReady(player1, new AsariCaptain());
        addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, asariCaptain)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, asariCaptain)).isEqualTo(3);
    }

    @Test
    void nonSamuraiOrWarriorDoesNotTriggerTheAbility() {
        Permanent asariCaptain = addCreatureReady(player1, new AsariCaptain());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, asariCaptain)).isEqualTo(4);
    }

    @Test
    void samuraiOrWarriorAttackingWithAnotherCreatureDoesNotTriggerTheAbility() {
        Permanent asariCaptain = addCreatureReady(player1, new AsariCaptain());
        addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, asariCaptain)).isEqualTo(4);
    }
}
