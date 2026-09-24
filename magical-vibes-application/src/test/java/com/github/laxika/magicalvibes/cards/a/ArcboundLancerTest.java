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
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundLancer.class, CrazedGoblin.class, DarksteelGargoyle.class,
        DarksteelPendant.class, Oxidize.class})
class ArcboundLancerTest extends BaseCardTest {

    @Test
    void entersWithFourPlusOnePlusOneCounters() {
        harness.castFromHand(player1, new ArcboundLancer(), "{7}");
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player1, "Arcbound Lancer");
        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent lancer = addCreatureReady(player1, new ArcboundLancer());
        lancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());

        destroyLancer(lancer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId())
                .doesNotContain(goblin.getId(), pendant.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void modularMayPutItsCountersOnOpponentsArtifactCreatureWhenItDies() {
        Permanent lancer = addCreatureReady(player1, new ArcboundLancer());
        lancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        destroyLancer(lancer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void modularMayBeDeclined() {
        Permanent lancer = addCreatureReady(player1, new ArcboundLancer());
        lancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyLancer(lancer);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularTriggersWhenItHasNoCounters() {
        Permanent lancer = addCreatureReady(player1, new ArcboundLancer());
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyLancer(lancer);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lancer.getCard());
    }

    @Test
    void firstStrikeDealsCombatDamageBeforeNonFirstStrikeCreature() {
        Permanent lancer = addCreatureReady(player1, new ArcboundLancer());
        lancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent goblin = addCreatureReady(player2, new CrazedGoblin());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lancer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(goblin);
    }

    private void destroyLancer(Permanent lancer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, lancer.getId());
    }
}
