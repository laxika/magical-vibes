package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TigerSharkAbyssalHunter.class, GrizzlyBears.class, Mountain.class})
class TigerSharkAbyssalHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering connives and adds a counter for a nonland discard")
    void enteringConnives() {
        harness.setHand(player1, List.of(new TigerSharkAbyssalHunter(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCardMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent shark = findPermanent(player1, "Tiger Shark, Abyssal Hunter");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Attacking connives")
    void attackingConnives() {
        Permanent shark = addReadyShark();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        discardByName("Grizzly Bears");

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("The activated ability makes Tiger Shark unblockable until end of turn")
    void abilityMakesItUnblockableUntilEndOfTurn() {
        Permanent shark = addReadyShark();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shark.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shark.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Entering with black hybrid mana still connives, and a land discard adds no counter")
    void enteringWithBlackManaAndDiscardingLand() {
        harness.setHand(player1, List.of(new TigerSharkAbyssalHunter(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        discardByName("Mountain");

        Permanent shark = findPermanent(player1, "Tiger Shark, Abyssal Hunter");
        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The ability accepts black mana without tapping and affects only Tiger Shark")
    void abilityAcceptsBlackManaAndAffectsOnlySelf() {
        Permanent shark = addReadyShark();
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shark.isCantBeBlocked()).isTrue();
        assertThat(shark.isTapped()).isFalse();
        assertThat(other.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Connive uses Tiger Shark's current controller when control changes before resolution")
    void conniveUsesCurrentController() {
        harness.setHand(player1, List.of(new TigerSharkAbyssalHunter(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        addCardMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent shark = findPermanent(player1, "Tiger Shark, Abyssal Hunter");
        gd.playerBattlefields.get(player1.getId()).remove(shark);
        gd.playerBattlefields.get(player2.getId()).add(shark);

        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mountain", "Grizzly Bears");
        List<Card> hand = gd.playerHands.get(player2.getId());
        int discardIndex = java.util.stream.IntStream.range(0, hand.size())
                .filter(i -> hand.get(i).getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.handleCardChosen(player2, discardIndex);

        assertThat(shark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyShark() {
        return addCreatureReady(player1, new TigerSharkAbyssalHunter());
    }

    private void addCardMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
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
