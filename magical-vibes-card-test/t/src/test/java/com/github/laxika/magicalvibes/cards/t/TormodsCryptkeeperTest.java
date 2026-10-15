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

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TormodsCryptkeeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Tormod's Cryptkeeper");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting your own graveyard exiles the sacrificed Cryptkeeper too")
    void canExileOwnGraveyardIncludingSacrificedSource() {
        var source = new TormodsCryptkeeper();
        var graveyardCard = new TormodsCryptkeeper();
        var opponentCard = new TormodsCryptkeeper();
        addCreatureReady(player1, source);
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard, source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(graveyardCard, source);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty graveyard is a legal target and sacrifice is still paid")
    void canTargetEmptyGraveyard() {
        addCreatureReady(player1, new TormodsCryptkeeper());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tormod's Cryptkeeper");
        harness.assertInGraveyard(player1, "Tormod's Cryptkeeper");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cards entering the targeted graveyard before resolution are also exiled")
    void exilesCardsPresentAtResolution() {
        addCreatureReady(player1, new TormodsCryptkeeper());
        var firstCard = new TormodsCryptkeeper();
        var laterCard = new TormodsCryptkeeper();
        harness.setGraveyard(player2, List.of(firstCard));

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.setGraveyard(player2, List.of(firstCard, laterCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(firstCard, laterCard);
        harness.assertInGraveyard(player1, "Tormod's Cryptkeeper");
    }

    @Test
    @DisplayName("Vigilance leaves Cryptkeeper untapped and able to activate after attacking")
    void canActivateAfterAttackingWithVigilance() {
        var cryptkeeper = addCreatureReady(player1, new TormodsCryptkeeper());
        var graveyardCard = new TormodsCryptkeeper();
        harness.setGraveyard(player2, List.of(graveyardCard));

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(cryptkeeper.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tormod's Cryptkeeper");
        harness.assertInGraveyard(player1, "Tormod's Cryptkeeper");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }
}
