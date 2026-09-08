package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QiqirnMerchant.class, Forest.class})
class QiqirnMerchantTest extends BaseCardTest {

    @Test
    void lootsWhenActivated() {
        Permanent merchant = addReadyMerchant();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(merchant.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDrawsThreeCards() {
        addReadyMerchant();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Qiqirn Merchant");
    }

    @Test
    void townsReduceSacrificeAbilityCost() {
        addReadyMerchant();
        addTown();
        addTown();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyMerchant() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new QiqirnMerchant());
        merchant.setSummoningSick(false);
        return merchant;
    }

    private void addTown() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new Forest());
        TestCards.mutableCard(town).setSubtypes(List.of(CardSubtype.TOWN));
    }
}
