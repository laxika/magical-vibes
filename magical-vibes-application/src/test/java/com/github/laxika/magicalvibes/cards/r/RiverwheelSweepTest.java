package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverwheelSweep.class, Forest.class, GrizzlyBears.class, Island.class})
class RiverwheelSweepTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature, puts three stun counters on it, and grants next-turn play permission")
    void tapsAndStunsCreatureAndExilesCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new Island();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RiverwheelSweep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
    }

    @Test
    void alreadyTappedCreatureStillGetsThreeStunCountersWithAnEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setTapped(true);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RiverwheelSweep()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player1);
            assertThat(target.isTapped()).isTrue();
            assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void singleCardLibraryAllowsPlayingTheExiledLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new RiverwheelSweep()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.castFromExile(player1, land.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
    }

    @Test
    void mustChooseExactlyOneOfTheTwoExiledCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new Island();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RiverwheelSweep()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void doesNotExileCardsWhenItsOnlyTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new Island();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RiverwheelSweep()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof RiverwheelSweep);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RiverwheelSweep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
