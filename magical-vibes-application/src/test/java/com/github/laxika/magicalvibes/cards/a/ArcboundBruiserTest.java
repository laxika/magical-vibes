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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArcboundBruiserTest extends BaseCardTest {

    @Test
    void entersWithThreePlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ArcboundBruiser()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bruiser = findPermanent(player1, "Arcbound Bruiser");
        assertThat(bruiser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(3);
    }

    @Test
    void deathTriggerMayPutItsCountersOnTargetArtifactCreature() {
        Permanent bruiser = addCreatureReady(player1, new ArcboundBruiser());
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bruiser.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyBruiser(player2, bruiser.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void deathTriggerCannotTargetNonArtifactCreature() {
        Permanent bruiser = addCreatureReady(player1, new ArcboundBruiser());
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bruiser.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyBruiser(player2, bruiser.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyBruiser(com.github.laxika.magicalvibes.model.Player destroyer, UUID bruiserId) {
        harness.forceActivePlayer(destroyer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(destroyer, List.of(new Assassinate()));
        harness.addMana(destroyer, ManaColor.BLACK, 1);
        harness.addMana(destroyer, ManaColor.COLORLESS, 3);

        gs.playCard(gd, destroyer, 0, 0, bruiserId, null);
        harness.passBothPriorities();
    }
}
