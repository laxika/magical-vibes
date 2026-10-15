package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RedRoomRecruit.class, GrizzlyBears.class, Mountain.class})
class RedRoomRecruitTest extends BaseCardTest {

    @Test
    void entersAndConnivesWithNonlandDiscard() {
        harness.setHand(player1, List.of(new RedRoomRecruit(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent recruit = findPermanent(player1, "Red Room Recruit");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void doesNotPutCounterOnItAfterLandDiscard() {
        harness.setHand(player1, List.of(new RedRoomRecruit(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent recruit = findPermanent(player1, "Red Room Recruit");
        discardByName("Mountain");

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    void canDiscardNonlandAlreadyInHandInsteadOfDrawnLand() {
        RedRoomRecruit discardedRecruit = new RedRoomRecruit();
        harness.setHand(player1, List.of(new RedRoomRecruit(), discardedRecruit));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent recruit = findPermanent(player1, "Red Room Recruit");
        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Red Room Recruit", "Mountain");
        harness.handleCardChosen(player1, 0);

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedRecruit);
    }

    @Test
    void onlyEnteringRecruitGetsConniveCounter() {
        Permanent existingRecruit = harness.addToBattlefieldAndReturn(player1, new RedRoomRecruit());
        RedRoomRecruit enteringCard = new RedRoomRecruit();
        harness.setHand(player1, List.of(enteringCard, new Mountain()));
        harness.setLibrary(player1, List.of(new RedRoomRecruit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        discardByName("Red Room Recruit");

        Permanent enteringRecruit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(existingRecruit.getId()))
                .findFirst().orElseThrow();
        assertThat(enteringRecruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existingRecruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
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
