package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.r.RishadanAirship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreditVoucher.class, DartingMerfolk.class, FreshVolunteers.class, RishadanAirship.class})
class CreditVoucherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Credit Voucher shuffles a chosen number of hand cards and draws that many")
    void shufflesChosenCardsAndDrawsThatMany() {
        CreditVoucher voucher = new CreditVoucher();
        Card first = new FreshVolunteers();
        Card second = new DartingMerfolk();
        Card kept = new RishadanAirship();
        harness.addToBattlefield(player1, voucher);
        harness.setHand(player1, List.of(first, second, kept));
        harness.setLibrary(player1, List.of(
                new FreshVolunteers(), new DartingMerfolk(), new RishadanAirship(), new FreshVolunteers()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(kept);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        harness.assertInGraveyard(player1, "Credit Voucher");
    }

    @Test
    @DisplayName("Choosing zero leaves the hand and library unchanged after the sacrifice")
    void choosingZeroDoesNothing() {
        CreditVoucher voucher = new CreditVoucher();
        Card first = new FreshVolunteers();
        Card second = new DartingMerfolk();
        List<Card> library = List.of(new RishadanAirship(), new FreshVolunteers());
        harness.addToBattlefield(player1, voucher);
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(librarySizeBefore)
                .containsExactlyInAnyOrderElementsOf(library);
        harness.assertInGraveyard(player1, "Credit Voucher");
    }

    @Test
    @DisplayName("Choosing every card in hand is allowed and draws the same number")
    void canChooseEveryCardInHand() {
        CreditVoucher voucher = new CreditVoucher();
        Card first = new FreshVolunteers();
        Card second = new DartingMerfolk();
        Card third = new RishadanAirship();
        List<Card> initialHand = List.of(first, second, third);
        List<Card> initialLibrary = List.of(
                new FreshVolunteers(), new DartingMerfolk(), new RishadanAirship(), new FreshVolunteers());
        harness.addToBattlefield(player1, voucher);
        harness.setHand(player1, initialHand);
        harness.setLibrary(player1, initialLibrary);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, initialHand.size());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.minCount()).isEqualTo(initialHand.size());
                    assertThat(choice.maxCount()).isEqualTo(initialHand.size());
                    assertThat(choice.validCardIds())
                            .containsExactly(first.getId(), second.getId(), third.getId());
                });
        harness.handleMultipleCardsChosen(player1, initialHand.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHand.size());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(initialLibrary.size());
    }

    @Test
    @DisplayName("A tapped Credit Voucher cannot be activated")
    void cannotActivateWhenTapped() {
        Permanent voucher = harness.addToBattlefieldAndReturn(player1, new CreditVoucher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        voucher.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(voucher);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Credit Voucher cannot be activated without two mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent voucher = harness.addToBattlefieldAndReturn(player1, new CreditVoucher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(voucher);
        assertThat(voucher.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty hand resolves without opening a card-choice prompt")
    void emptyHandResolvesWithoutPrompt() {
        CreditVoucher voucher = new CreditVoucher();
        List<Card> library = List.of(new FreshVolunteers(), new DartingMerfolk());
        harness.addToBattlefield(player1, voucher);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        harness.assertInGraveyard(player1, "Credit Voucher");
    }
}
