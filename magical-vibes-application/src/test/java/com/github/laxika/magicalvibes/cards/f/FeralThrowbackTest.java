package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Brontotherium;
import com.github.laxika.magicalvibes.cards.e.Earthblighter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralThrowback.class, Brontotherium.class, Earthblighter.class})
class FeralThrowbackTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters for each of two Beast cards revealed from hand")
    void entersWithCountersForBeastCardsInHand() {
        harness.setHand(player1, List.of(
                new FeralThrowback(), new Brontotherium(), new Brontotherium(), new Earthblighter()));
        addManaForFeralThrowback();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Feral Throwback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only Beast cards in the controller's hand")
    void ignoresOtherHandsAndNonBeastCards() {
        harness.setHand(player1, List.of(new FeralThrowback(), new Earthblighter()));
        harness.setHand(player2, List.of(new Brontotherium()));
        addManaForFeralThrowback();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Feral Throwback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May reveal only some of the Beast cards for amplify")
    void choosesSubsetOfBeastCardsForAmplify() {
        FeralThrowback throwback = new FeralThrowback();
        Brontotherium firstBeast = new Brontotherium();
        Brontotherium secondBeast = new Brontotherium();
        harness.setHand(player1, List.of(throwback, firstBeast, secondBeast, new Earthblighter()));
        addManaForFeralThrowback();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBeast.getId(), secondBeast.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBeast.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Feral Throwback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Provoke untaps the chosen creature and forces it to block")
    void provokeUntapsAndForcesBlock() {
        Permanent throwback = addCreatureReady(player1, new FeralThrowback());
        Permanent blocker = addCreatureReady(player2, new Earthblighter());
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

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining provoke leaves the chosen creature unchanged")
    void decliningProvokeDoesNothing() {
        addCreatureReady(player1, new FeralThrowback());
        Permanent blocker = addCreatureReady(player2, new Earthblighter());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Provoke only offers a defending player's creature")
    void provokeFiltersTargets() {
        addCreatureReady(player1, new FeralThrowback());
        Permanent ownCreature = addCreatureReady(player1, new Earthblighter());
        Permanent defendingCreature = addCreatureReady(player2, new Earthblighter());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Provoke has no target prompt when the defending player controls no creatures")
    void provokeWithoutLegalTargetDoesNotPrompt() {
        addCreatureReady(player1, new FeralThrowback());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void addManaForFeralThrowback() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
