package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChangeOfPlans.class, Forest.class, GrizzlyBears.class, Mountain.class, Unsummon.class})
class ChangeOfPlansTest extends BaseCardTest {

    @Test
    void connivesEachTargetAndPhasesOutChosenTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans(), new GrizzlyBears(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addManaForXTwo();

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, first.getId());
        discardByName("Grizzly Bears");
        discardByName("Mountain");

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).extracting(Permanent::getId)
                .containsExactly(first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second);
    }

    @Test
    void canPhaseOutNoneAndLandDiscardDoesNotAddCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForXOne();

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void onlyCreaturesYouControlCanBeTargeted() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans()));
        addManaForXOne();

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresExactlyXTargetsWhenCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans()));
        addManaForXTwo();

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastWithZeroTargetsWhenXIsZero() {
        harness.setHand(player1, List.of(new ChangeOfPlans(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Change of Plans");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    void controllerChoosesWhichCreatureConnivesFirstDuringResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans(), new GrizzlyBears(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addManaForXTwo();

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).as("controller chooses the next creature to connive during resolution")
                .isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        discardByName("Grizzly Bears");

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canPhaseOutEveryTargetAndTheyReturnOnlyOnControllersNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans(), new GrizzlyBears(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addManaForXTwo();

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        discardByName("Grizzly Bears");
        discardByName("Mountain");
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(first, second);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlySurvivingLegalTargetConnivesAndCanPhaseOut() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        addManaForXTwo();

        harness.castInstantForX(player1, 0, 2, List.of(removed.getId(), survivor.getId()));
        harness.castInstant(player2, 0, removed.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(survivor.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(survivor.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(survivor);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotConniveWhenEveryTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChangeOfPlans()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        addManaForXOne();

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Change of Plans");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }
    private void addManaForXOne() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addManaForXTwo() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
