package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMagicMirror.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheMagicMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each instant or sorcery card in its controller's graveyard")
    void costReductionCountsInstantAndSorceryCards() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TheMagicMirror()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Adds a knowledge counter, then draws for each knowledge counter at upkeep")
    void upkeepAddsCounterThenDrawsForAllKnowledgeCounters() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new TheMagicMirror());
        mirror.setCounterCount(CounterType.KNOWLEDGE, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest())));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mirror.getCounterCount(CounterType.KNOWLEDGE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("No maximum hand size prevents cleanup discard")
    void noMaximumHandSizePreventsCleanupDiscard() {
        harness.addToBattlefield(player1, new TheMagicMirror());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }
}
