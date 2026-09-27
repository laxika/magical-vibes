package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundPrototype.class, Assassinate.class, BronzeSable.class, GrizzlyBears.class})
class ArcboundPrototypeTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ArcboundPrototype()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent prototype = findPermanent(player1, "Arcbound Prototype");
        assertThat(prototype.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreature() {
        Permanent prototype = addCreatureReady(player1, new ArcboundPrototype());
        prototype.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prototype.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyPrototype(prototype.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularCannotTargetNonArtifactCreature() {
        Permanent prototype = addCreatureReady(player1, new ArcboundPrototype());
        prototype.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prototype.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyPrototype(prototype.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyPrototype(UUID prototypeId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, prototypeId, null);
        harness.passBothPriorities();
    }
}
