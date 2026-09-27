package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundSlasher.class, DoomBlade.class, GrizzlyBears.class, Ornithopter.class})
class ArcboundSlasherTest extends BaseCardTest {

    @Test
    void entersWithFourModularCountersAndRiotCanAddAnother() {
        Permanent slasher = castSlasher();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(slasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(5);
    }

    @Test
    void riotCanGiveHasteInsteadOfAnotherCounter() {
        Permanent slasher = castSlasher();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(slasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, slasher, Keyword.HASTE)).isTrue();
    }

    @Test
    void modularDeathMayMoveCountersToAnArtifactCreature() {
        Permanent slasher = addCreatureReady(player1, new ArcboundSlasher());
        slasher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        destroySlasher(slasher);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ornithopter.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent castSlasher() {
        harness.setHand(player1, List.of(new ArcboundSlasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Arcbound Slasher");
    }

    private void destroySlasher(Permanent slasher) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player2, 0, 0, slasher.getId(), null);
        harness.passBothPriorities();
    }
}
