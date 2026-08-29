package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcavatedWallTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}: Controller mills one card")
    void controllerMillsOneCard() {
        Permanent wall = addReadyWall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> library = gd.playerDecks.get(player1.getId());
        while (library.size() > 1) {
            library.removeFirst();
        }
        Card topCard = library.getFirst();

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
        Permanent wall = addReadyWall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerDecks.get(player1.getId()).clear();

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

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new ExcavatedWall());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
