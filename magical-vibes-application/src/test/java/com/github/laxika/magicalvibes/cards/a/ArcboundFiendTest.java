package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcboundFiend.class, CrazedGoblin.class, DarksteelGargoyle.class, Oxidize.class})
class ArcboundFiendTest extends BaseCardTest {

    @Test
    void entersWithThreePlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ArcboundFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent fiend = findPermanent(player1, "Arcbound Fiend");
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void upkeepMayMoveCounterFromTargetCreatureOntoFiend() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent goblin = addCreatureReady(player2, new CrazedGoblin());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void upkeepMayBeDeclined() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void upkeepMayMoveCounterButDoesNothingWhenTargetHasNone() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularMayPutCountersOnTargetArtifactCreature() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());

        destroyFiend(fiend);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId()).doesNotContain(goblin.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularMayBeDeclined() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyFiend(fiend);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fearPreventsNonblackNonartifactCreatureFromBlocking() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent goblin = addCreatureReady(player2, new CrazedGoblin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(goblin),
                gd.playerBattlefields.get(player1.getId()).indexOf(fiend)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Arcbound Fiend (fear)");
    }

    @Test
    void fearAllowsArtifactCreatureToBlock() {
        Permanent fiend = addCreatureReady(player1, new ArcboundFiend());
        fiend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(gargoyle),
                gd.playerBattlefields.get(player1.getId()).indexOf(fiend))));

        assertThat(gargoyle.isBlocking()).isTrue();
    }

    private void destroyFiend(Permanent fiend) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, fiend.getId());
    }
}
