package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullRaid.class, Island.class, TamiyoCollectorOfTales.class})
class SkullRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two chosen cards")
    void discardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new Island(), new Island(), new Island())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island())));
        castSkullRaid();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Controller draws the difference when target has fewer than two cards")
    void controllerDrawsShortfall() {
        harness.setHand(player2, new ArrayList<>(List.of(new Island())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island())));
        castSkullRaid();

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller draws two cards when target has an empty hand")
    void emptyTargetHandDrawsTwo() {
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island())));
        castSkullRaid();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Skull Raid cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SkullRaid()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @CardUsed({SkullRaid.class, Island.class, TamiyoCollectorOfTales.class})
    @DisplayName("Controller draws two when the opponent cannot discard")
    void drawsTwoWhenDiscardIsPrevented() {
        harness.addToBattlefield(player2, new TamiyoCollectorOfTales());
        Island first = new Island();
        Island second = new Island();
        harness.setHand(player2, List.of(first, second));
        Island drawFirst = new Island();
        Island drawSecond = new Island();
        harness.setLibrary(player1, List.of(drawFirst, drawSecond));
        castSkullRaid();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawFirst, drawSecond);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void foretellsAndCastsOnALaterTurn() {
        SkullRaid spell = new SkullRaid();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Island()));
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(spell.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Skull Raid");
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        SkullRaid spell = new SkullRaid();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        SkullRaid spell = new SkullRaid();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    private void castSkullRaid() {
        harness.setHand(player1, List.of(new SkullRaid()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
