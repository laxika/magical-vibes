package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.z.ZabazTheGlimmerwasp;
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

@CardUsed({ArcboundWhelp.class, BronzeSable.class, DoomBlade.class, GrizzlyBears.class, OrnithopterOfParadise.class, ZabazTheGlimmerwasp.class})
class ArcboundWhelpTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        Permanent whelp = castWhelp();

        assertThat(whelp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void redAbilityBoostsUntilEndOfTurn() {
        Permanent whelp = castWhelp();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(whelp.getPowerModifier()).isZero();
    }

    @Test
    void modularDeathMayMoveCountersToAnArtifactCreature() {
        Permanent whelp = addCreatureReady(player1, new ArcboundWhelp());
        whelp.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyWhelp(whelp);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularCanBeDeclined() {
        Permanent recipient = prepareModularTarget(false, 2);
        chooseModularTarget(recipient, false);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularCanTargetAnOpponentsArtifactCreature() {
        Permanent recipient = prepareModularTarget(true, 4);
        chooseModularTarget(recipient, true);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void zabazAddsOneToModularCountersButNotEntryCounters() {
        Permanent recipient = prepareModularTarget(false, 2);
        chooseModularTarget(recipient, true);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularPromptDescribesPlusOnePlusOneCounters() {
        Permanent recipient = prepareModularTarget(false, 2);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("+1/+1").doesNotContain("-1/-1");
    }

    private Permanent prepareModularTarget(boolean opponentControlsRecipient, int counters) {
        harness.castFromHand(player1, new ZabazTheGlimmerwasp(), "{1}");
        harness.passBothPriorities();
        Permanent whelp = castWhelp();
        assertThat(whelp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        whelp.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        Permanent recipient = addCreatureReady(opponentControlsRecipient ? player2 : player1,
                new OrnithopterOfParadise());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, whelp.getId());
        harness.passBothPriorities();
        return recipient;
    }

    private void chooseModularTarget(Permanent recipient, boolean accept) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
    private Permanent castWhelp() {
        harness.castFromHand(player1, new ArcboundWhelp(), "{3}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Arcbound Whelp");
    }

    private void destroyWhelp(Permanent whelp) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, whelp.getId());
    }
}
