package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowDayTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two creatures, locks their next untap, then draws two and discards one")
    void tapsCreaturesAndDrawsThenDiscards() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        Card drawnForest = new Forest();
        Card drawnIsland = new Island();
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland));
        harness.setHand(player1, List.of(new SnowDay(), discarded));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnForest, drawnIsland);
        harness.assertInGraveyard(player1, "Snow Day");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("May choose only one creature")
    void mayChooseOneCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new SnowDay(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SnowDay()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
