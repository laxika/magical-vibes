package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "When you remove the last [kind] counter from this permanent" ({@code Triggers.youRemoveLastCounterFromThis}) is a
 * triggered ability of its own: it triggers when its controller removes the last counter of that kind, however that
 * happens, and goes on the stack as one object of all its effects (CR 603.3).
 */
class LastCounterRemovedTriggerTest extends BaseCardTest {

    private Permanent relic(int chargeCounters) {
        Card card = new Card();
        card.setName("Fading Relic");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        card.addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RemoveCounterFromSourceEffect(CounterType.CHARGE, 1));
        card.addTrigger(Triggers.youRemoveLastCounterFromThis(CounterType.CHARGE), new DrawCardEffect(1),
                new GainLifeEffect(1));
        Permanent relic = harness.addToBattlefieldAndReturn(player1, card);
        relic.setCounterCount(CounterType.CHARGE, chargeCounters);
        return relic;
    }

    private List<StackEntry> relicEntries() {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals("Fading Relic")).toList();
    }

    private static PermanentCounterSupport counterSupport() {
        return GameTestEngineContext.get().getBean(PermanentCounterSupport.class);
    }

    @Test
    @DisplayName("Its controller removing the last counter of the kind puts the ability on the stack as one object")
    void controllerRemovingTheLastCounterTriggers() {
        Permanent relic = relic(1);

        harness.inMutationScope(() -> {
            counterSupport().removeCounterFromPermanent(gd, relic, CounterType.CHARGE, 1);
            counterSupport().notifyLastCounterRemoved(gd, relic, CounterType.CHARGE, 1, player1.getId());
        });

        assertThat(relicEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getEventCounterType()).isEqualTo(CounterType.CHARGE);
        });
    }

    @Test
    @DisplayName("A counter of the kind being left, another kind's last counter or another player's removal doesn't trigger")
    void otherRemovalsDoNotTrigger() {
        Permanent relic = relic(2);
        relic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(() -> {
            counterSupport().removeCounterFromPermanent(gd, relic, CounterType.CHARGE, 1);
            counterSupport().notifyLastCounterRemoved(gd, relic, CounterType.CHARGE, 1, player1.getId());
            counterSupport().removeCounterFromPermanent(gd, relic, CounterType.PLUS_ONE_PLUS_ONE, 1);
            counterSupport().notifyLastCounterRemoved(gd, relic, CounterType.PLUS_ONE_PLUS_ONE, 1, player1.getId());
            counterSupport().removeCounterFromPermanent(gd, relic, CounterType.CHARGE, 1);
            counterSupport().notifyLastCounterRemoved(gd, relic, CounterType.CHARGE, 1, player2.getId());
        });

        assertThat(relicEntries()).isEmpty();
    }
}
