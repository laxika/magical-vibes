package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlaughterSingerTest extends BaseCardTest {

    @Test
    @DisplayName("Another toxic creature gets +1/+1 when it attacks")
    void boostsAnotherToxicAttacker() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent toxicCreature = addCreatureReady(player1, new CrawlingChorus());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-toxic creature does not trigger the ability")
    void doesNotBoostNonToxicAttacker() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent nonToxicCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nonToxicCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonToxicCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slaughter Singer does not trigger for itself")
    void doesNotBoostItselfWhenItAttacks() {
        Permanent singer = addCreatureReady(player1, new SlaughterSinger());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent toxicCreature = addCreatureReady(player1, new CrawlingChorus());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(1);
    }
}
