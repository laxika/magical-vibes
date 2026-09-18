package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KilnmouthDragon.class, FugitiveWizard.class})
class KilnmouthDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters for each revealed Dragon card")
    void entersWithThreeCountersPerRevealedDragon() {
        KilnmouthDragon card = new KilnmouthDragon();
        KilnmouthDragon firstDragon = new KilnmouthDragon();
        KilnmouthDragon secondDragon = new KilnmouthDragon();
        harness.setHand(player1, List.of(card, firstDragon, secondDragon, new FugitiveWizard()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstDragon.getId(), secondDragon.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstDragon.getId(), secondDragon.getId()));
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Kilnmouth Dragon");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("The controller may reveal only some of the Dragon cards")
    void entersWithCountersForSelectedDragons() {
        KilnmouthDragon card = new KilnmouthDragon();
        KilnmouthDragon firstDragon = new KilnmouthDragon();
        KilnmouthDragon secondDragon = new KilnmouthDragon();
        harness.setHand(player1, List.of(card, firstDragon, secondDragon));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstDragon.getId()));
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Kilnmouth Dragon");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only revealed Dragon cards from its controller's hand")
    void ignoresOtherHandsAndAllowsRevealingZeroDragons() {
        KilnmouthDragon card = new KilnmouthDragon();
        KilnmouthDragon ownDragon = new KilnmouthDragon();
        KilnmouthDragon opponentDragon = new KilnmouthDragon();
        harness.setHand(player1, List.of(card, ownDragon, new FugitiveWizard()));
        harness.setHand(player2, List.of(opponentDragon));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownDragon.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Kilnmouth Dragon");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Deals damage equal to its +1/+1 counters to any target player")
    void dealsCounterDamageToPlayer() {
        Permanent dragon = addCreatureReady(player1, new KilnmouthDragon());
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(dragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals damage equal to its +1/+1 counters to any target creature")
    void dealsCounterDamageToCreature() {
        Permanent dragon = addCreatureReady(player1, new KilnmouthDragon());
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = addCreatureReady(player2, new FugitiveWizard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

}
