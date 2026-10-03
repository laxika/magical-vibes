package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudboundMoogle.class, GrizzlyBears.class, Plains.class})
class CloudboundMoogleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entering Moogle can target itself with its counter trigger")
    void etbCanPutCounterOnItself() {
        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent moogle = findPermanent(player1, "Cloudbound Moogle");
        harness.handlePermanentChosen(player1, moogle.getId());
        resolveAllTriggers();

        assertThat(moogle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingFindsPlains() {
        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cloudbound Moogle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Plains");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Plainscycling pays its discard cost before the search resolves")
    void plainscyclingDiscardsAsActivationCost() {
        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Cloudbound Moogle");
        harness.assertInGraveyard(player1, "Cloudbound Moogle");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Plainscycling can fail to find even when a Plains is available")
    void plainscyclingCanFailToFind() {
        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Cloudbound Moogle");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerLibraries.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Plainscycling resolves without drawing when the library contains no Plains")
    void plainscyclingWithoutMatchingCard() {
        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.setLibrary(player1, List.of(new CloudboundMoogle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Cloudbound Moogle");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling cannot discard the card without paying two mana")
    void plainscyclingRequiresFullManaCost() {
        harness.setHand(player1, List.of(new CloudboundMoogle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Cloudbound Moogle");
        harness.assertNotInGraveyard(player1, "Cloudbound Moogle");
        assertThat(gd.stack).isEmpty();
    }
}
