package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepyPuppeteer.class, GrizzlyBears.class})
class CreepyPuppeteerTest extends BaseCardTest {

    @Test
    void exactlyTwoAttackersOfferToSetTheOtherAttackerToFourThree() {
        addCreatureReady(player1, new CreepyPuppeteer());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void acceptingSetsOnlyTheOtherAttackerToFourThreeUntilEndOfTurn() {
        addCreatureReady(player1, new CreepyPuppeteer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void decliningLeavesTheOtherAttackerUnchanged() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new CreepyPuppeteer());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenPuppeteerIsNotAttacking() {
        addCreatureReady(player1, new CreepyPuppeteer());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerWithMoreThanOneOtherAttacker() {
        addCreatureReady(player1, new CreepyPuppeteer());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
