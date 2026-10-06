package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AutomaticLibrarian;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

@CardUsed({ImpedeMomentum.class, AutomaticLibrarian.class, Island.class})
class ImpedeMomentumTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature, puts three stun counters on it, and scries one")
    void tapsStunsAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());
        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, java.util.List.of(new ImpedeMomentum()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alreadyTappedCreatureStillGetsThreeMoreStunCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());
        target.tap();
        target.setCounterCount(CounterType.STUN, 2);
        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void stunCountersReplaceThreeUntapStepsBeforeCreatureUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());
        harness.setLibrary(player1, List.of());
        cast(target);

        harness.performUntapStep(player1);
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canKeepScryedCardOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());
        Card top = new Island();
        Card next = new ImpedeMomentum();
        harness.setLibrary(player1, List.of(top, next));
        cast(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        harness.assertInGraveyard(player1, "Impede Momentum");
    }

    @Test
    void canPutScryedCardOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());
        Card top = new Island();
        Card next = new ImpedeMomentum();
        harness.setLibrary(player1, List.of(top, next));
        cast(target);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        harness.assertInGraveyard(player1, "Impede Momentum");
    }

    @Test
    void emptyLibraryDoesNotPreventTappingAndStunning() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());
        harness.setLibrary(player1, List.of());
        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Impede Momentum");
    }

    @Test
    void removedTargetPreventsScrying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new ImpedeMomentum()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.assertInGraveyard(player1, "Impede Momentum");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, java.util.List.of(new ImpedeMomentum()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
