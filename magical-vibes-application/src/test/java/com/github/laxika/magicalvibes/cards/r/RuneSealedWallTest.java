package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuneSealedWall.class})
class RuneSealedWallTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the wall surveils 1 and may put the top card into the graveyard")
    void tapsToSurveil() {
        Card topCard = new RuneSealedWall();
        harness.setLibrary(player1, List.of(topCard));
        Permanent wall = addCreatureReady(player1, new RuneSealedWall());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveil may leave the top card on top of the library")
    void mayLeaveTopCardOnLibrary() {
        Card topCard = new RuneSealedWall();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new RuneSealedWall());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Defender prevents an otherwise ready wall from attacking")
    void cannotAttackWithDefender() {
        Permanent wall = addCreatureReady(player1, new RuneSealedWall());

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick wall cannot pay the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new RuneSealedWall());
        wall.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped wall cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent wall = addCreatureReady(player1, new RuneSealedWall());
        wall.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library resolves without a choice or a draw")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent wall = addCreatureReady(player1, new RuneSealedWall());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("The ability still surveils its controller's library after the wall leaves")
    void resolvesAfterWallLeavesBattlefield() {
        Card topCard = new RuneSealedWall();
        Card secondCard = new RuneSealedWall();
        Card opponentCard = new RuneSealedWall();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentCard));
        Permanent wall = addCreatureReady(player1, new RuneSealedWall());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        gd.playerGraveyards.get(player1.getId()).add(wall.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
    }
}
