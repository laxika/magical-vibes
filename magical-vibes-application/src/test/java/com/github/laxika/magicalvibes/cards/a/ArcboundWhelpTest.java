package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ArcboundWhelp.class, BronzeSable.class, DoomBlade.class, GrizzlyBears.class})
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

    private Permanent castWhelp() {
        harness.setHand(player1, List.of(new ArcboundWhelp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Arcbound Whelp");
    }

    private void destroyWhelp(Permanent whelp) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player2, 0, 0, whelp.getId(), null);
        harness.passBothPriorities();
    }
}
