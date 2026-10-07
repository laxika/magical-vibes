package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarksIngenuity.class, GrizzlyBears.class, Naturalize.class})
class StarksIngenuityTest extends BaseCardTest {

    @Test
    void entersDrawsPaidXAndCountersEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new StarksIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        prepareMainPhase();

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentDrawDoesNotTriggerAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StarksIngenuity());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canEnchantOnlyCreatureYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StarksIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void canDeclineEntryPayment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new StarksIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        prepareMainPhase();

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(findPermanent(player1, "Stark's Ingenuity").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void laterDrawCountersOnlyEnchantedCreatureAfterTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StarksIngenuity());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        prepareMainPhase();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void pendingDrawTriggerStillCountersCreatureAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StarksIngenuity());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
