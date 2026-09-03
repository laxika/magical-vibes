package com.github.laxika.magicalvibes.cards.k;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({Kaboom.class, Forest.class, GrizzlyBears.class, Island.class})
class KaboomTest extends BaseCardTest {

    @Test
    void revealsAndDealsSeparatelyForEachTarget() {
        Card forest = new Forest();
        Card firstBears = new GrizzlyBears();
        Card island = new Island();
        Card secondBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, firstBears, island, secondBears));
        harness.setHand(player1, List.of(new Kaboom()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        PendingInteraction.LibraryReorder firstReorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(firstReorder.cards()).containsExactly(forest, firstBears);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        PendingInteraction.LibraryReorder secondReorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(secondReorder.cards()).containsExactly(island, secondBears);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(firstBears, forest, secondBears, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canResolveWithNoTargets() {
        harness.setHand(player1, List.of(new Kaboom()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetCreature() {
        var creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Kaboom()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
