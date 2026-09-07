package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaffinesSilencer.class, GrizzlyBears.class, Mountain.class, Shock.class})
class RaffinesSilencerTest extends BaseCardTest {

    @Test
    void enteringConnivesAndAddsCounterForNonlandDiscard() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(silencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringConnivesWithoutCounterForLandDiscard() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        discardByName("Mountain");

        assertThat(silencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathTriggerUsesSilencersPowerAndTargetsOnlyOpponentCreatures() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new Shock(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        discardByName("Grizzly Bears");

        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        UUID silencerId = silencer.getId();
        UUID opponentBearsId = opponentBears.getId();
        UUID ownBearsId = ownBears.getId();

        harness.castInstant(player1, 0, silencerId);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentBearsId);
        assertThat(choice.validIds()).doesNotContain(ownBearsId);

        harness.handlePermanentChosen(player1, opponentBearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentBearsId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addSilencerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
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
