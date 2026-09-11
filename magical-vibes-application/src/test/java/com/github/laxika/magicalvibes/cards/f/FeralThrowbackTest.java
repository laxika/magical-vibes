package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Brontotherium;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralThrowback.class, Brontotherium.class, GrizzlyBears.class})
class FeralThrowbackTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters for two Beast cards revealed from hand")
    void entersWithCountersForBeastCardsInHand() {
        harness.setHand(player1, List.of(
                new FeralThrowback(), new Brontotherium(), new Brontotherium(), new GrizzlyBears()));
        addManaForFeralThrowback();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent throwback = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FeralThrowback)
                .findFirst()
                .orElseThrow();
        assertThat(throwback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Beast cards in the controller's hand")
    void ignoresOtherHandsAndNonBeastCards() {
        harness.setHand(player1, List.of(new FeralThrowback(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Brontotherium()));
        addManaForFeralThrowback();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent throwback = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FeralThrowback)
                .findFirst()
                .orElseThrow();
        assertThat(throwback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Provoke untaps the chosen creature and forces it to block")
    void provokeUntapsAndForcesBlock() {
        Permanent throwback = addCreatureReady(player1, new FeralThrowback());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(blocker.getId());

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getMustBlockIds()).containsExactly(throwback.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private void addManaForFeralThrowback() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
