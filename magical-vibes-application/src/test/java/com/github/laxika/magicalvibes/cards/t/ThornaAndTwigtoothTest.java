package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornaAndTwigtooth.class, FutureSight.class, GrizzlyBears.class})
class ThornaAndTwigtoothTest extends BaseCardTest {

    @Test
    void entersWithTwoMinusOneMinusOneCounters() {
        harness.setHand(player1, List.of(new ThornaAndTwigtooth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thorna = findPermanent(player1, "Thorna and Twigtooth");
        assertThat(thorna.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackRemovesAllCountersDrainsAndBoostsOnlyTopCreatureCard() {
        Permanent thorna = addCreatureReady(player1, new ThornaAndTwigtooth());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target.setCounterCount(CounterType.QUEST, 2);
        harness.addToBattlefield(player1, new FutureSight());

        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice.validIds()).contains(target.getId()).doesNotContain(player2.getId());
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();
        });

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        List<Permanent> enteredBears = bears.stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .toList();
        assertThat(enteredBears).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, enteredBears.get(0))).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enteredBears.get(0))).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, enteredBears.get(1))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteredBears.get(1))).isEqualTo(2);
    }
}
