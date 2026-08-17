package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreditVoucherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Credit Voucher shuffles a chosen number of hand cards and draws that many")
    void shufflesChosenCardsAndDrawsThatMany() {
        CreditVoucher voucher = new CreditVoucher();
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card shock = new Shock();
        harness.addToBattlefield(player1, voucher);
        harness.setHand(player1, List.of(bears, giant, shock));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant(), new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        harness.assertInGraveyard(player1, "Credit Voucher");
    }

    @Test
    @DisplayName("Choosing zero leaves the hand and library unchanged after the sacrifice")
    void choosingZeroDoesNothing() {
        CreditVoucher voucher = new CreditVoucher();
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        harness.addToBattlefield(player1, voucher);
        harness.setHand(player1, List.of(bears, giant));
        harness.setLibrary(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, giant);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        harness.assertInGraveyard(player1, "Credit Voucher");
    }
}
