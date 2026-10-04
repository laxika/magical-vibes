package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExcavatedWall.class})
class ExcavatedWallTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}: Controller mills one card")
    void controllerMillsOneCard() {
        Permanent wall = addCreatureReady(player1, new ExcavatedWall());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card topCard = new ExcavatedWall();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Mill does nothing when the controller's library is empty")
    void millDoesNothingWhenLibraryIsEmpty() {
        Permanent wall = addCreatureReady(player1, new ExcavatedWall());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while Excavated Wall has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ExcavatedWall());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    void millsOnlyTopCardOnResolutionAndLeavesOpponentLibraryAlone() {
        addCreatureReady(player1, new ExcavatedWall());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card top = new ExcavatedWall();
        Card next = new ExcavatedWall();
        Card opponentTop = new ExcavatedWall();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent wall = addCreatureReady(player1, new ExcavatedWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateTappedWall() {
        Permanent wall = addCreatureReady(player1, new ExcavatedWall());
        wall.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent wall = addCreatureReady(player1, new ExcavatedWall());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Card top = new ExcavatedWall();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        harness.setGraveyard(player1, List.of(wall.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wall.getCard(), top);
    }
}
