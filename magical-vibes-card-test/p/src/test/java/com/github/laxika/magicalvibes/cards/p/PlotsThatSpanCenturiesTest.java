package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.cards.i.ICallForSlaughter;
import com.github.laxika.magicalvibes.cards.m.MyLaughterEchoes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RegisterNextSchemeSetInMotionReplacementEffect;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlotsThatSpanCenturies.class, MyLaughterEchoes.class, ICallForSlaughter.class})
class PlotsThatSpanCenturiesTest extends BaseCardTest {

    @Test
    void nextSchemeSetInMotionIsRepeatedThreeTimesAndReplacementIsConsumed() {
        Permanent plots = harness.addToBattlefieldAndReturn(player1, new PlotsThatSpanCenturies());
        PlotsThatSpanCenturies firstScheme = new PlotsThatSpanCenturies();
        StackEntry firstEntry = schemeEntry(firstScheme);
        gd.stack.add(firstEntry);
        harness.inMutationScope(() -> ActionEvents.schemeSetInMotion(gd, firstEntry));
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(plots);
        harness.addToBattlefield(player1, new MyLaughterEchoes());

        Card nextScheme = new ICallForSlaughter();
        StackEntry nextEntry = schemeEntry(nextScheme);
        gd.stack.add(nextEntry);
        harness.inMutationScope(() -> ActionEvents.schemeSetInMotion(gd, nextEntry));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof MyLaughterEchoes)
                .hasSize(3);

        gd.stack.clear();
        StackEntry finalEntry = schemeEntry(new ICallForSlaughter());
        gd.stack.add(finalEntry);
        harness.inMutationScope(() -> ActionEvents.schemeSetInMotion(gd, finalEntry));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof MyLaughterEchoes)
                .hasSize(1);
        gd.stack.clear();
    }

    @Test
    void replacementLastsUntilUsedAndSurvivesTheCleanupStep() {
        Permanent plots = harness.addToBattlefieldAndReturn(player1, new PlotsThatSpanCenturies());
        StackEntry firstEntry = schemeEntry(new PlotsThatSpanCenturies());
        gd.stack.add(firstEntry);
        harness.inMutationScope(() -> ActionEvents.schemeSetInMotion(gd, firstEntry));
        resolveAllTriggers();

        assertThat(gd.floatingEffectsOnPlayer(player1.getId(),
                RegisterNextSchemeSetInMotionReplacementEffect.class)).hasSize(1);

        gd.rulesEvents.occur(new TurnEvent.Cleanup(gd.activePlayerId));

        assertThat(gd.floatingEffectsOnPlayer(player1.getId(),
                RegisterNextSchemeSetInMotionReplacementEffect.class)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(plots);
        harness.addToBattlefield(player1, new MyLaughterEchoes());
        gd.stack.clear();
        StackEntry nextEntry = schemeEntry(new ICallForSlaughter());
        gd.stack.add(nextEntry);
        harness.inMutationScope(() -> ActionEvents.schemeSetInMotion(gd, nextEntry));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof MyLaughterEchoes).hasSize(3);
        assertThat(gd.floatingEffectsOfTypes(RegisterNextSchemeSetInMotionReplacementEffect.class)).isEmpty();
        gd.stack.clear();
    }

    private StackEntry schemeEntry(Card scheme) {
        return TestStackDoor.triggered(scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects());
    }
}
