package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChangeOfPlans.class, Forest.class, GrizzlyBears.class, Mountain.class})
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

        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(target);
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

    private void addManaForXOne() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addManaForXTwo() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
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
