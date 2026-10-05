package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParcelMyr.class})
class ParcelMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Parcel Myr sacrifices it and draws a card")
    void activateAbilitySacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new ParcelMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gameData = harness.getGameData();
        int handSizeBefore = gameData.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Parcel Myr");
        harness.assertInGraveyard(player1, "Parcel Myr");
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A tapped summoning-sick Parcel Myr can be sacrificed on the opponent's turn with colored mana")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        var myr = harness.addToBattlefieldAndReturn(player1, new ParcelMyr());
        myr.setSummoningSick(true);
        myr.tap();
        ParcelMyr topCard = new ParcelMyr();
        ParcelMyr nextCard = new ParcelMyr();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 2);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Parcel Myr");
        harness.assertInGraveyard(player1, "Parcel Myr");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Parcel Myr cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new ParcelMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Parcel Myr");
    }
}
