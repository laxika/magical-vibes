package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MobLookout.class, Mountain.class})
class MobLookoutTest extends BaseCardTest {

    @Test
    void connivesTargetCreatureAndAddsCounterForNonlandDiscard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MobLookout());
        harness.setHand(player1, List.of(new MobLookout(), new Mountain()));
        harness.setLibrary(player1, List.of(new MobLookout()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mob Lookout");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Mountain");
    }

    @Test
    void conniveDoesNotAddCounterForLandDiscard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MobLookout());
        harness.setHand(player1, List.of(new MobLookout(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        resolveAllTriggers();

        discardByName("Mountain");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Mountain");
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MobLookout());
        harness.setHand(player1, List.of(new MobLookout()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void canDiscardACardAlreadyInHandInsteadOfTheDrawnCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MobLookout());
        harness.setHand(player1, List.of(new MobLookout(), new MobLookout()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        discardByName("Mob Lookout");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mob Lookout");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotConniveWhenTargetLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MobLookout());
        harness.setHand(player1, List.of(new MobLookout(), new Mountain()));
        harness.setLibrary(player1, List.of(new MobLookout()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerStillConnivesWhenMobLookoutLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MobLookout());
        MobLookout sourceCard = new MobLookout();
        harness.setHand(player1, List.of(sourceCard, new Mountain()));
        harness.setLibrary(player1, List.of(new MobLookout()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == sourceCard)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();
        discardByName("Mob Lookout");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
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
