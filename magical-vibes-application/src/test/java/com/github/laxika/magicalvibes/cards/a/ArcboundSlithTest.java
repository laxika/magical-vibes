package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
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

@CardUsed({ArcboundSlith.class, CrazedGoblin.class, DarksteelGargoyle.class, Oxidize.class})
class ArcboundSlithTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArcboundSlith()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent slith = findPermanent(player1, "Arcbound Slith");
        assertThat(slith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsACounterWhenItDealsCombatDamageToAPlayer() {
        Permanent slith = addCreatureReady(player1, new ArcboundSlith());
        slith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        slith.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(slith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent slith = addCreatureReady(player1, new ArcboundSlith());
        slith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());

        destroySlith(slith);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId()).doesNotContain(goblin.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularMayDeclineToPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent slith = addCreatureReady(player1, new ArcboundSlith());
        slith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroySlith(slith);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularCanPutAllItsCountersOnAnOpponentsArtifactCreature() {
        Permanent slith = addCreatureReady(player1, new ArcboundSlith());
        slith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        destroySlith(slith);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroySlith(Permanent slith) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, slith.getId());
    }
}
