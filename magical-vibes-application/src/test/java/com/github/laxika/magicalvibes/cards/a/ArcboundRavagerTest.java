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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundRavager.class, CrazedGoblin.class, DarksteelGargoyle.class,
        DarksteelPendant.class, Oxidize.class})
class ArcboundRavagerTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArcboundRavager()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ravager = findPermanent(player1, "Arcbound Ravager");
        assertThat(ravager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingAnArtifactPutsACounterOnRavager() {
        Permanent ravager = addCreatureReady(player1, new ArcboundRavager());
        ravager.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(artifact.getId(), ravager.getId())
                .doesNotContain(goblin.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(ravager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent ravager = addCreatureReady(player1, new ArcboundRavager());
        ravager.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());

        destroyRavager(ravager);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId())
                .doesNotContain(goblin.getId(), pendant.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularCanPutItsCountersOnAnOpponentsArtifactCreature() {
        Permanent ravager = addCreatureReady(player1, new ArcboundRavager());
        ravager.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        destroyRavager(ravager);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularMayBeDeclined() {
        Permanent ravager = addCreatureReady(player1, new ArcboundRavager());
        ravager.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyRavager(ravager);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularTriggersWhenItHasNoCounters() {
        Permanent ravager = addCreatureReady(player1, new ArcboundRavager());
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ravager.getCard());
    }

    private void destroyRavager(Permanent ravager) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, ravager.getId());
    }
}
