package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
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

@CardUsed({ArcboundStinger.class, AuriokGlaivemaster.class, DarksteelGargoyle.class, Oxidize.class})
class ArcboundStingerTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArcboundStinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stinger = findPermanent(player1, "Arcbound Stinger");
        assertThat(stinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularMayPutItsCounterOnTargetArtifactCreatureWhenItDies() {
        Permanent stinger = addCreatureReady(player1, new ArcboundStinger());
        stinger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyStinger(stinger);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularCannotTargetNonArtifactCreature() {
        Permanent stinger = addCreatureReady(player1, new ArcboundStinger());
        stinger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent nonArtifactCreature = addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyStinger(stinger);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId())
                .doesNotContain(nonArtifactCreature.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularCanPutItsCounterOnOpponentsArtifactCreature() {
        Permanent stinger = addCreatureReady(player1, new ArcboundStinger());
        stinger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        destroyStinger(stinger);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularTriggersWhenItHasNoCounters() {
        addCreatureReady(player1, new ArcboundStinger());
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyStinger(Permanent stinger) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, stinger.getId());
    }
}
