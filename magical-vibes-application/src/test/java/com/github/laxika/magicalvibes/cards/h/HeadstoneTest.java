package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SpectralBears;
import com.github.laxika.magicalvibes.cards.w.WinterSky;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Headstone.class, SpectralBears.class, WinterSky.class})
class HeadstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target card from an opponent's graveyard")
    void exilesCardFromOpponentGraveyard() {
        Card bears = new SpectralBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Spectral Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spectral Bears"));
    }

    @Test
    @DisplayName("Can exile a card from the controller's own graveyard, any card type")
    void exilesNonCreatureCardFromOwnGraveyard() {
        Card winterSky = new WinterSky();
        harness.setGraveyard(player1, new ArrayList<>(List.of(winterSky)));
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, winterSky.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Winter Sky");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Winter Sky"));
    }

    @Test
    @DisplayName("Schedules a draw for the caster instead of drawing immediately")
    void schedulesDrawForCaster() {
        Card bears = new SpectralBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Card bears = new SpectralBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Does not schedule the draw when the graveyard target is illegal on resolution")
    void doesNotScheduleDrawWhenTargetRemoved() {
        Card bears = new SpectralBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, bears.getId());

        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target card leaves the graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Card bears = new SpectralBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, bears.getId());

        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot be cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new Headstone()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
