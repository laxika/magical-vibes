package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassSecretary.class})
class BrassSecretaryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Brass Secretary sacrifices it and draws a card")
    void activateAbilitySacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new BrassSecretary());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Brass Secretary");
        harness.assertInGraveyard(player1, "Brass Secretary");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Brass Secretary cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BrassSecretary());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Brass Secretary");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Brass Secretary can activate on the opponent's turn with colored mana")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent secretary = harness.addToBattlefieldAndReturn(player1, new BrassSecretary());
        secretary.tap();
        secretary.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        BrassSecretary cardToDraw = new BrassSecretary();
        harness.setLibrary(player1, List.of(cardToDraw, new BrassSecretary()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Brass Secretary");
        harness.assertInGraveyard(player1, "Brass Secretary");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToDraw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
