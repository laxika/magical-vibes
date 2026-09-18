package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SylvokExplorer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundWanderer.class, Arachnoid.class, SylvokExplorer.class})
class ArcboundWandererTest extends BaseCardTest {

    @Test
    void sunburstPutsOneCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new ArcboundWanderer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Arcbound Wanderer");
        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new ArcboundWanderer()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Arcbound Wanderer");
        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent wanderer = addCreatureReady(player1, new ArcboundWanderer());
        wanderer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        wanderer.setMarkedDamage(4);
        Permanent arachnoid = addCreatureReady(player1, new Arachnoid());
        Permanent explorer = addCreatureReady(player1, new SylvokExplorer());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(arachnoid.getId()).doesNotContain(explorer.getId());

        harness.handlePermanentChosen(player1, arachnoid.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(arachnoid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void modularMayBeDeclined() {
        Permanent wanderer = addCreatureReady(player1, new ArcboundWanderer());
        wanderer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        wanderer.setMarkedDamage(4);
        Permanent arachnoid = addCreatureReady(player1, new Arachnoid());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, arachnoid.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(arachnoid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularMayPutItsCountersOnOpponentsArtifactCreature() {
        Permanent wanderer = addCreatureReady(player1, new ArcboundWanderer());
        wanderer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        wanderer.setMarkedDamage(4);
        Permanent arachnoid = addCreatureReady(player2, new Arachnoid());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(arachnoid.getId());

        harness.handlePermanentChosen(player1, arachnoid.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(arachnoid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void modularStillTriggersWhenItHasNoCounters() {
        Permanent wanderer = addCreatureReady(player1, new ArcboundWanderer());
        Permanent arachnoid = addCreatureReady(player1, new Arachnoid());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(arachnoid.getId());

        harness.handlePermanentChosen(player1, arachnoid.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(arachnoid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
