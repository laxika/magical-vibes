package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class ThranFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating exiles Thran Foundry and taps it")
    void activatingExilesSelfAndTapsIt() {
        Permanent foundry = addReadyFoundry(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Thran Foundry");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Thran Foundry"));
        assertThat(foundry.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Target player shuffles their graveyard into their library")
    void targetPlayerShufflesTheirGraveyard() {
        addReadyFoundry(player1);
        Card bear = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player2, List.of(bear, spider));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        int targetLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetLibrarySize + 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can target its controller")
    void canTargetController() {
        addReadyFoundry(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        int librarySize = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize + 1);
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void cannotTargetPermanent() {
        addReadyFoundry(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyFoundry(Player player) {
        Permanent foundry = new Permanent(new ThranFoundry());
        foundry.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(foundry);
        return foundry;
    }
}
