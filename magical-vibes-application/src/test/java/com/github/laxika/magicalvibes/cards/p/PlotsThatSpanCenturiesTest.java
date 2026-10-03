package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.ICallForSlaughter;
import com.github.laxika.magicalvibes.cards.m.MyLaughterEchoes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
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
        harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, firstEntry);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(plots);
        harness.addToBattlefield(player1, new MyLaughterEchoes());

        Card nextScheme = new ICallForSlaughter();
        StackEntry nextEntry = schemeEntry(nextScheme);
        gd.stack.add(nextEntry);
        harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, nextEntry);

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof MyLaughterEchoes)
                .hasSize(3);

        gd.stack.clear();
        StackEntry finalEntry = schemeEntry(new ICallForSlaughter());
        gd.stack.add(finalEntry);
        harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, finalEntry);

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof MyLaughterEchoes)
                .hasSize(1);
        gd.stack.clear();
    }

    private StackEntry schemeEntry(Card scheme) {
        return new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL));
    }
}
