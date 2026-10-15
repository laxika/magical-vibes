package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BackstreetBruiser;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.q.QuickDrawDagger;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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

@CardUsed({PsychicPickpocket.class, BackstreetBruiser.class, Mountain.class,
        RayOfCommand.class, Murder.class, QuickDrawDagger.class})
class PsychicPickpocketTest extends BaseCardTest {

    @Test
    void enteringConnivesThenReturnsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackstreetBruiser());
        castWithDrawnCard(new BackstreetBruiser(), new Mountain());

        Permanent pickpocket = findPermanent(player1, "Psychic Pickpocket");
        discardByName("Backstreet Bruiser");

        assertThat(pickpocket.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Backstreet Bruiser");
    }

    @Test
    void enteringReturnsTargetEvenWhenLandIsDiscarded() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackstreetBruiser());
        castWithDrawnCard(new Mountain(), new BackstreetBruiser());

        Permanent pickpocket = findPermanent(player1, "Psychic Pickpocket");
        discardByName("Mountain");

        assertThat(pickpocket.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Backstreet Bruiser");
    }

    @Test
    void enteringCannotReturnAland() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackstreetBruiser());
        castWithDrawnCard(new BackstreetBruiser(), new Mountain());

        discardByName("Backstreet Bruiser");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Backstreet Bruiser");
    }

    private void castWithDrawnCard(Card drawnCard, Card cardToKeep) {
        harness.castFromHand(player1, new PsychicPickpocket(), "{4}{U}");
        harness.setHand(player1, List.of(cardToKeep));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void canChooseNoBounceTargetAfterConniving() {
        harness.addToBattlefield(player2, new BackstreetBruiser());
        castWithDrawnCard(new BackstreetBruiser(), new Mountain());
        discardByName("Backstreet Bruiser");

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Backstreet Bruiser");
        harness.assertOnBattlefield(player1, "Psychic Pickpocket");
        assertThat(findPermanent(player1, "Psychic Pickpocket")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canReturnItselfAfterConniving() {
        castWithDrawnCard(new BackstreetBruiser(), new Mountain());
        discardByName("Backstreet Bruiser");

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Psychic Pickpocket"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Psychic Pickpocket");
        harness.assertInHand(player1, "Psychic Pickpocket");
    }

    @Test
    void currentCreatureControllerConnivesButOriginalAbilityControllerChoosesBounce() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackstreetBruiser());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new RayOfCommand(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new BackstreetBruiser()));
        Permanent pickpocket = harness.enterBattlefieldAndReturn(player1, new PsychicPickpocket());

        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, pickpocket.getId());
        harness.assertOnBattlefield(player2, "Psychic Pickpocket");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Backstreet Bruiser");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player2, 1);
        assertThat(pickpocket.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Backstreet Bruiser");
        harness.assertInHand(player2, "Backstreet Bruiser");
    }

    @Test
    void returnsNoncreaturePermanentAfterConniving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuickDrawDagger());
        castWithDrawnCard(new BackstreetBruiser(), new Mountain());
        discardByName("Backstreet Bruiser");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Quick-Draw Dagger");
        harness.assertInHand(player2, "Quick-Draw Dagger");
    }

    @Test
    void stillConnivesAndBouncesAfterLeavingBattlefieldBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackstreetBruiser());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new BackstreetBruiser()));
        harness.setHand(player2, List.of(new Murder()));
        Permanent pickpocket = harness.enterBattlefieldAndReturn(player1, new PsychicPickpocket());

        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, pickpocket.getId());
        harness.assertInGraveyard(player1, "Psychic Pickpocket");
        harness.passBothPriorities();
        discardByName("Backstreet Bruiser");

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Backstreet Bruiser");
        harness.assertInHand(player2, "Backstreet Bruiser");
        harness.assertInGraveyard(player1, "Backstreet Bruiser");
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
