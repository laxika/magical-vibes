package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sewerdreg.class, GrizzlyBears.class})
class SewerdregTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and exiles a target card from an opponent's graveyard")
    void sacrificesItselfAndExilesCardFromOpponentsGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new Sewerdreg());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sewerdreg");
        harness.assertInGraveyard(player1, "Sewerdreg");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(exiledCards(player2)).contains(target);
    }

    @Test
    @DisplayName("Can exile a target card from its controller's graveyard")
    void exilesCardFromOwnGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new Sewerdreg());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(exiledCards(player1)).contains(target);
    }

    @Test
    @DisplayName("Cannot target a card that is not in a graveyard")
    void cannotTargetBattlefieldCard() {
        Card target = new GrizzlyBears();
        harness.addToBattlefield(player2, target);
        harness.addToBattlefield(player1, new Sewerdreg());

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    private List<Card> exiledCards(Player player) {
        GameData gameData = harness.getGameData();
        return gameData.getPlayerExiledCards(player.getId());
    }
}
