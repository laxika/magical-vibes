package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StartingTown;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QiqirnMerchant.class, Forest.class, StartingTown.class})
class QiqirnMerchantTest extends BaseCardTest {

    @Test
    void lootsWhenActivated() {
        Permanent merchant = addCreatureReady(player1, new QiqirnMerchant());
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
        addCreatureReady(player1, new QiqirnMerchant());
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
        addCreatureReady(player1, new QiqirnMerchant());
        addTown();
        addTown();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void lootingWithEmptyHandDiscardsTheDrawnCard() {
        addCreatureReady(player1, new QiqirnMerchant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void mayDiscardTheDrawnCardInsteadOfTheOriginalCard() {
        addCreatureReady(player1, new QiqirnMerchant());
        Forest original = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    void sacrificeIsPaidBeforeCardsAreDrawn() {
        addCreatureReady(player1, new QiqirnMerchant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Qiqirn Merchant");
        harness.assertInGraveyard(player1, "Qiqirn Merchant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void excessTownsReduceManaCostToZeroButStillRequireSacrifice() {
        addCreatureReady(player1, new QiqirnMerchant());
        for (int i = 0; i < 8; i++) {
            addTown();
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Qiqirn Merchant");
        harness.assertInGraveyard(player1, "Qiqirn Merchant");
    }

    @Test
    void opponentTownsDoNotReduceActivationCost() {
        Permanent merchant = addCreatureReady(player1, new QiqirnMerchant());
        harness.addToBattlefield(player2, new StartingTown());
        harness.addToBattlefield(player2, new StartingTown());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(merchant.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Qiqirn Merchant");
        harness.assertNotInGraveyard(player1, "Qiqirn Merchant");
    }

    @Test
    void townDiscountDoesNotApplyToLootingAbility() {
        Permanent merchant = addCreatureReady(player1, new QiqirnMerchant());
        addTown();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(merchant.isTapped()).isFalse();
    }

    @Test
    void summoningSicknessPreventsBothTapAbilities() {
        harness.addToBattlefield(player1, new QiqirnMerchant());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Qiqirn Merchant");
        harness.assertNotInGraveyard(player1, "Qiqirn Merchant");
    }

    private void addTown() {
        harness.addToBattlefield(player1, new StartingTown());
    }
}
