package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuturistForge.class, Island.class})
class FuturistForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void enteringBattlefieldDrawsCard() {
        Card drawn = new Island();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new FuturistForge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Paying four mana and sacrificing it draws two cards")
    void sacrificeAbilityDrawsTwoCards() {
        Card firstDrawn = new Island();
        Card secondDrawn = new Island();
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new FuturistForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId()))
                .containsExactly(firstDrawn, secondDrawn);
        harness.assertNotOnBattlefield(player1, "Futurist Forge");
        harness.assertInGraveyard(player1, "Futurist Forge");
    }

    @Test
    @DisplayName("Sacrifice is paid before the ability resolves and draws cards")
    void sacrificeIsPaidBeforeDrawing() {
        Card firstDrawn = new Island();
        Card secondDrawn = new Island();
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new FuturistForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Futurist Forge");
        harness.assertInGraveyard(player1, "Futurist Forge");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
    }

    @Test
    @DisplayName("A tapped Forge can activate during an opponent's turn")
    void tappedForgeCanActivateOnOpponentsTurn() {
        Card firstDrawn = new Island();
        Card secondDrawn = new Island();
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn));
        harness.setHand(player1, List.of());
        harness.addToBattlefieldAndReturn(player1, new FuturistForge()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
        harness.assertInGraveyard(player1, "Futurist Forge");
    }

    @Test
    @DisplayName("Four colorless mana cannot pay the blue activation cost")
    void activationRequiresBlueMana() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new FuturistForge());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Futurist Forge");
        harness.assertNotInGraveyard(player1, "Futurist Forge");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

}
