package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreasureTrove.class, RagingGoblin.class})
class TreasureTroveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability with mana draws a card")
    void activatingDrawsACard() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of(new RagingGoblin()));
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Raging Goblin");
    }

    @Test
    @DisplayName("Drawing from an empty library loses the game")
    void drawingFromEmptyLibraryLosesTheGame() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("no cards to draw")).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated repeatedly since it does not tap")
    void canActivateRepeatedly() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Requires two blue mana in addition to the generic cost")
    void requiresTwoBlueMana() {
        Permanent trove = addTrove(player1);
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, trove), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the generic cost with colorless mana")
    void paysGenericCostWithColorlessMana() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent trove = addTrove(player1);
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, trove), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays mana immediately but draws only when the ability resolves")
    void drawsOnlyOnResolution() {
        Permanent trove = addTrove(player1);
        RagingGoblin topCard = new RagingGoblin();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(trove.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can draw during the opponent's upkeep and only the controller draws")
    void canActivateDuringOpponentsTurn() {
        Permanent trove = addTrove(player1);
        RagingGoblin topCard = new RagingGoblin();
        RagingGoblin opponentsCard = new RagingGoblin();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    @DisplayName("Drawing the last card does not lose the game")
    void drawingLastCardDoesNotLose() {
        Permanent trove = addTrove(player1);
        RagingGoblin lastCard = new RagingGoblin();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(lastCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private Permanent addTrove(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TreasureTrove());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
