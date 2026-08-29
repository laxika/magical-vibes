package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NecrosquitoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two oil counters and gets +1/+1 for each oil counter")
    void entersWithOilCountersAndScalesWithOilCounters() {
        Permanent necrosquito = addNecrosquito();

        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, necrosquito)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, necrosquito)).isEqualTo(2);

        necrosquito.setCounterCount(CounterType.OIL, 4);

        assertThat(gqs.getEffectivePower(gd, necrosquito)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, necrosquito)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets an oil counter when a creature or artifact you control dies")
    void getsOilCounterWhenOwnCreatureOrArtifactDies() {
        Permanent necrosquito = addNecrosquito();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(3);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature or a noncreature nonartifact permanent")
    void ignoresOpponentCreatureAndNoncreatureNonartifact() {
        Permanent necrosquito = addNecrosquito();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AuraOfSilence());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(2);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    private Permanent addNecrosquito() {
        harness.setHand(player1, List.of(new Necrosquito()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Necrosquito");
    }
}
