package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WakerOfWaves.class, GrizzlyBears.class, Shock.class})
class WakerOfWavesTest extends BaseCardTest {

    @Test
    @DisplayName("Debuffs creatures opponents control")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new WakerOfWaves());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hand ability discards Waker and puts one of the top two cards into hand")
    void handAbilitySelectsTopCardAndGraveyardsTheOther() {
        Card topCard = new GrizzlyBears();
        Card otherCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, otherCard));
        harness.setHand(player1, List.of(new WakerOfWaves()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(otherCard.getId()));

        harness.assertInGraveyard(player1, "Waker of Waves");
        assertThat(gd.playerHands.get(player1.getId())).contains(otherCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hand ability must put one card into hand when two cards are available")
    void cannotDeclinePuttingACardIntoHand() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new WakerOfWaves()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }

    @Test
    @DisplayName("With one card in the library, put that card into hand")
    void handAbilityWithOneLibraryCard() {
        Card onlyCard = new Shock();
        WakerOfWaves waker = new WakerOfWaves();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(waker));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(waker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(waker);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With an empty library, the hand ability resolves without drawing or losing")
    void handAbilityWithEmptyLibrary() {
        WakerOfWaves waker = new WakerOfWaves();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(waker));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(waker);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Multiple Wakers stack their debuffs and affect creatures entering later")
    void debuffsStackAndApplyToNewCreatures() {
        harness.addToBattlefield(player1, new WakerOfWaves());
        harness.addToBattlefield(player1, new WakerOfWaves());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
    }
}
