package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormodsCryptkeeper.class, GoblinHero.class, ScarwoodGoblins.class})
class TormodsCryptkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability exiles target player's graveyard")
    void activateAbilityExilesGraveyard() {
        addCreatureReady(player1, new TormodsCryptkeeper());
        harness.setGraveyard(player2, List.of(new GoblinHero(), new ScarwoodGoblins()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the creature")
    void activateAbilitySacrificesSelf() {
        addCreatureReady(player1, new TormodsCryptkeeper());
        harness.setGraveyard(player2, List.of(new GoblinHero()));

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Tormod's Cryptkeeper");
        harness.assertInGraveyard(player1, "Tormod's Cryptkeeper");
    }

    @Test
    @DisplayName("Cannot activate the ability while the creature is tapped")
    void cannotActivateWhenTapped() {
        var cryptkeeper = addCreatureReady(player1, new TormodsCryptkeeper());
        cryptkeeper.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
