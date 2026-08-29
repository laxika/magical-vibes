package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent wanderer = addCreatureReady(player1, new ArcboundWanderer());
        wanderer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        wanderer.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyWanderer(wanderer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void destroyWanderer(Permanent wanderer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, wanderer.getId(), null);
        harness.passBothPriorities();
    }
}
