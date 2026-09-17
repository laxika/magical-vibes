package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BargainingTable;
import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HammerMage.class, BargainingTable.class, CreditVoucher.class,
        FreshVolunteers.class, IronLance.class})
class HammerMageTest extends BaseCardTest {

    @Test
    void destroysArtifactsWithManaValueAtMostXAcrossAllBattlefields() {
        Permanent hammerMage = addCreatureReady(player1, new HammerMage());
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new CreditVoucher());
        harness.addToBattlefield(player2, new IronLance());
        harness.addToBattlefield(player2, new BargainingTable());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 2, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(hammerMage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Hammer Mage", "Fresh Volunteers");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Bargaining Table");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Fresh Volunteers", "Credit Voucher");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Iron Lance");
    }

    @Test
    void activationRequiresDiscardingACard() {
        Permanent hammerMage = addCreatureReady(player1, new HammerMage());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
        assertThat(hammerMage.isTapped()).isFalse();
    }

    @Test
    void zeroXLeavesPositiveManaValueArtifactsIntact() {
        addCreatureReady(player1, new HammerMage());
        harness.addToBattlefield(player2, new CreditVoucher());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Hammer Mage");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Credit Voucher");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Fresh Volunteers");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
