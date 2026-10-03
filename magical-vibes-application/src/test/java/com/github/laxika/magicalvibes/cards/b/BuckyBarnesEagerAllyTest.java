package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DaredevilManWithoutFear;
import com.github.laxika.magicalvibes.cards.e.EnlistmentOfficer;
import com.github.laxika.magicalvibes.cards.s.SHIELDSpyKit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        BuckyBarnesEagerAlly.class,
        DaredevilManWithoutFear.class,
        EnlistmentOfficer.class,
        SHIELDSpyKit.class,
        Shock.class
})
class BuckyBarnesEagerAllyTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger offers one Equipment, Hero, or Soldier from the top four")
    void deathTriggerOffersEligibleCards() {
        SHIELDSpyKit equipment = new SHIELDSpyKit();
        DaredevilManWithoutFear hero = new DaredevilManWithoutFear();
        EnlistmentOfficer soldier = new EnlistmentOfficer();
        setupAndKillBucky(List.of(equipment, hero, soldier, new Shock()));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                equipment.getId(), hero.getId(), soldier.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Death trigger may decline and bottom all revealed cards")
    void deathTriggerMayDecline() {
        DaredevilManWithoutFear hero = new DaredevilManWithoutFear();
        setupAndKillBucky(List.of(hero, new Shock(), new Shock(), new Shock()));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hero);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Death trigger needs no choice when the top four have no eligible card")
    void deathTriggerWithNoEligibleCardNeedsNoChoice() {
        setupAndKillBucky(List.of(new Shock(), new Shock(), new Shock(), new Shock()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @CardUsed({BuckyBarnesEagerAlly.class, Shock.class})
    @DisplayName("Only the top four cards are considered and the rest go below untouched cards")
    void deathTriggerLeavesDeeperCardsOnTop() {
        BuckyBarnesEagerAlly chosen = new BuckyBarnesEagerAlly();
        Shock first = new Shock();
        Shock second = new Shock();
        Shock third = new Shock();
        BuckyBarnesEagerAlly fifth = new BuckyBarnesEagerAlly();
        Shock sixth = new Shock();
        setupAndKillBucky(List.of(chosen, first, second, third, fifth, sixth));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen).doesNotContain(fifth);
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({BuckyBarnesEagerAlly.class, Shock.class})
    @DisplayName("A short library still allows taking its only eligible card")
    void deathTriggerWithShortLibrary() {
        BuckyBarnesEagerAlly eligible = new BuckyBarnesEagerAlly();
        Shock other = new Shock();
        setupAndKillBucky(List.of(eligible, other));

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({BuckyBarnesEagerAlly.class, Shock.class})
    @DisplayName("Even the only card in the library may be declined")
    void deathTriggerMayDeclineOnlyCard() {
        BuckyBarnesEagerAlly eligible = new BuckyBarnesEagerAlly();
        setupAndKillBucky(List.of(eligible));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({BuckyBarnesEagerAlly.class, Shock.class})
    @DisplayName("An empty library finishes the death trigger without a choice or a draw")
    void deathTriggerWithEmptyLibrary() {
        setupAndKillBucky(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setupAndKillBucky(List<Card> library) {
        harness.setLibrary(player1, library);
        Permanent bucky = harness.addToBattlefieldAndReturn(player1, new BuckyBarnesEagerAlly());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bucky.getId());
        harness.passBothPriorities();
    }
}
