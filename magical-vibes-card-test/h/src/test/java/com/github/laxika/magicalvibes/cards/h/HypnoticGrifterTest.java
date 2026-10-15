package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrokersVeteran;
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

@CardUsed({HypnoticGrifter.class, BrokersVeteran.class, Mountain.class})
class HypnoticGrifterTest extends BaseCardTest {

    @Test
    void activatedAbilityConnivesAndAddsCounterForNonlandDiscard() {
        Permanent grifter = addReadyGrifter();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new BrokersVeteran()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Brokers Veteran");

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void activatedAbilityDoesNotAddCounterForLandDiscard() {
        Permanent grifter = addReadyGrifter();
        harness.setHand(player1, List.of(new BrokersVeteran()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mountain");

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Brokers Veteran");
    }

    @Test
    void conniveUsesCurrentControllerAfterControlChanges() {
        Permanent grifter = addReadyGrifter();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new BrokersVeteran()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new BrokersVeteran()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(grifter);
        gd.playerBattlefields.get(player2.getId()).add(grifter);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Brokers Veteran");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        PendingInteraction.DiscardChoice choice = (PendingInteraction.DiscardChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);
        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void conniveStillDrawsAndDiscardsWhenSourceHasLeftBattlefield() {
        Permanent grifter = addReadyGrifter();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new BrokersVeteran()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(grifter);
        gd.playerGraveyards.get(player1.getId()).add(grifter.getCard());
        harness.passBothPriorities();
        discardByName("Brokers Veteran");

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Hypnotic Grifter", "Brokers Veteran");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent grifter = harness.addToBattlefieldAndReturn(player1, new HypnoticGrifter());
        grifter.setSummoningSick(true);
        grifter.tap();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new BrokersVeteran()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        discardByName("Brokers Veteran");

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grifter.isTapped()).isTrue();
    }

    private Permanent addReadyGrifter() {
        Permanent grifter = harness.addToBattlefieldAndReturn(player1, new HypnoticGrifter());
        grifter.setSummoningSick(false);
        return grifter;
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
