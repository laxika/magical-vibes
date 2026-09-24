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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundShikari.class, BronzeSable.class, GrizzlyBears.class, Assassinate.class})
class ArcboundShikariTest extends BaseCardTest {

    @Test
    void entersWithTwoCountersAndCountersOtherArtifactCreatures() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new ArcboundShikari()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shikari = findPermanent(player1, "Arcbound Shikari");
        assertThat(shikari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonartifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreature() {
        Permanent shikari = addCreatureReady(player1, new ArcboundShikari());
        shikari.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        shikari.tap();
        Permanent artifactCreature = addCreatureReady(player1, new BronzeSable());
        Permanent nonartifactCreature = addCreatureReady(player1, new GrizzlyBears());

        destroyShikari(shikari);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(artifactCreature.getId())
                .doesNotContain(nonartifactCreature.getId());

        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroyShikari(Permanent shikari) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, shikari.getId(), null);
        harness.passBothPriorities();
    }
}
