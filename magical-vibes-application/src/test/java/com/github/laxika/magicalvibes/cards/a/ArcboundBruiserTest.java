package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelPendant;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
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

@CardUsed({ArcboundBruiser.class, CrazedGoblin.class, DarksteelGargoyle.class,
        DarksteelPendant.class, Oxidize.class})
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
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyBruiser(player2, bruiser.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void deathTriggerCanTargetOpponentsArtifactCreature() {
        Permanent bruiser = addCreatureReady(player1, new ArcboundBruiser());
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        destroyBruiser(player2, bruiser.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void deathTriggerCannotTargetNonArtifactCreature() {
        Permanent bruiser = addCreatureReady(player1, new ArcboundBruiser());
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());

        destroyBruiser(player2, bruiser.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId())
                .doesNotContain(goblin.getId(), pendant.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyBruiser(com.github.laxika.magicalvibes.model.Player destroyer, UUID bruiserId) {
        harness.forceActivePlayer(destroyer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(destroyer, List.of(new Oxidize()));
        harness.addMana(destroyer, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(destroyer, 0, bruiserId);
    }
}
