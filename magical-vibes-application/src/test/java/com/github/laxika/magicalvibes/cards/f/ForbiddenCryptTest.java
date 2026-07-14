package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddenCryptTest extends BaseCardTest {

    // ===== Draw replacement: return a card from graveyard instead of drawing =====

    @Test
    @DisplayName("Drawing returns a chosen card from the graveyard to hand instead of drawing")
    void drawReturnsCardFromGraveyardInsteadOfDrawing() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Island())));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        // The graveyard card is now in hand; the library was untouched (no draw happened).
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Drawing with an empty graveyard loses the game")
    void losesGameWhenGraveyardEmptyOnDraw() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Island())));
        harness.setGraveyard(player1, List.of());

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        // Can't return a card — player1 loses; no card was drawn from the library.
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Island"));
    }

    // ===== Graveyard replacement: cards are exiled instead of entering the graveyard =====

    @Test
    @DisplayName("Own card that would be put into the graveyard is exiled instead")
    void ownDyingCreatureIsExiledInsteadOfGraveyard() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exile replacement does not affect an opponent's graveyard")
    void doesNotAffectOpponentGraveyard() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
