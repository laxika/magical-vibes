package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicOfProgenitus.class, CylianElf.class, ResoundingThunder.class})
class RelicOfProgenitusTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target player exiles a card from their graveyard")
    void tapExilesTargetGraveyardCard() {
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of(new CylianElf()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Single card auto-exiles
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cylian Elf"));
    }

    @Test
    @DisplayName("{1}, Exile self: exile all graveyards and draw a card")
    void exileSelfExilesAllGraveyardsAndDraws() {
        RelicOfProgenitus relic = new RelicOfProgenitus();
        harness.addToBattlefield(player1, relic);
        harness.setGraveyard(player1, List.of(new ResoundingThunder()));
        harness.setGraveyard(player2, List.of(new CylianElf()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // All graveyards emptied
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // Controller drew a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Relic itself was exiled (no longer on the battlefield)
        harness.assertNotOnBattlefield(player1, "Relic of Progenitus");
    }

    @Test
    void targetedPlayerChoosesExactlyOneCardAtResolution() {
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        CylianElf elf = new CylianElf();
        ResoundingThunder thunder = new ResoundingThunder();
        harness.setGraveyard(player2, List.of(elf, thunder));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(elf, thunder);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(elf);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(thunder).doesNotContain(elf);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        CylianElf elf = new CylianElf();
        harness.setGraveyard(player1, List.of(elf));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elf);
    }

    @Test
    void canTargetPlayerWithEmptyGraveyardAndExilesCardAddedBeforeResolution() {
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        CylianElf elf = new CylianElf();
        harness.setGraveyard(player2, List.of(elf));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elf);
    }

    @Test
    void emptyTargetGraveyardDoesNothing() {
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Relic of Progenitus");
    }

    @Test
    void tappedRelicCanExileItselfAndDrawWithEmptyGraveyards() {
        RelicOfProgenitus relic = new RelicOfProgenitus();
        harness.addToBattlefield(player1, relic);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of());
        CylianElf elf = new CylianElf();
        harness.setLibrary(player1, List.of(elf));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Relic of Progenitus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(relic);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
