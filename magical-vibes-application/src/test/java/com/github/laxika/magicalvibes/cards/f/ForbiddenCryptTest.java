package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.InfernalContract;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.j.JunglePatrol;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForbiddenCrypt.class, DarkBanishing.class, Disenchant.class, InfernalContract.class,
        IronTuskElephant.class, Island.class, JunglePatrol.class})
class ForbiddenCryptTest extends BaseCardTest {

    // ===== Draw replacement: return a card from graveyard instead of drawing =====

    @Test
    @DisplayName("Drawing returns a chosen card from the graveyard to hand instead of drawing")
    void drawReturnsCardFromGraveyardInsteadOfDrawing() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        // The graveyard card is now in hand; the library was untouched (no draw happened).
        harness.assertInHand(player1, "Iron Tusk Elephant");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Forbidden Crypt does not replace an opponent's draw")
    void doesNotReplaceOpponentsDraw() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new IronTuskElephant()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player2, "Iron Tusk Elephant");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drawing with an empty graveyard loses the game")
    void losesGameWhenGraveyardEmptyOnDraw() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setGraveyard(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        // Can't return a card — player1 loses; no card was drawn from the library.
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertNotInHand(player1, "Island");
    }

    // ===== Graveyard replacement: cards are exiled instead of entering the graveyard =====

    @Test
    @DisplayName("Own card that would be put into the graveyard is exiled instead")
    void ownDyingCreatureIsExiledInsteadOfGraveyard() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.addToBattlefield(player1, new IronTuskElephant());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Iron Tusk Elephant");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player1, "Iron Tusk Elephant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Iron Tusk Elephant"))
                .anyMatch(c -> c.getName().equals("Dark Banishing"));
    }

    @Test
    @DisplayName("Exile replacement does not affect an opponent's graveyard")
    void doesNotAffectOpponentGraveyard() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.addToBattlefield(player2, new IronTuskElephant());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Iron Tusk Elephant");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Iron Tusk Elephant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Iron Tusk Elephant"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dark Banishing"));
    }

    @Test
    @DisplayName("Forbidden Crypt exiles itself when it would be put into its controller's graveyard")
    void exilesItselfInsteadOfGoingToGraveyard() {
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new ForbiddenCrypt());

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, crypt.getId());

        harness.assertNotOnBattlefield(player1, "Forbidden Crypt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forbidden Crypt"));
        harness.assertInGraveyard(player1, "Disenchant");
    }

    @Test
    @DisplayName("A multi-card draw applies the replacement separately to every card")
    void replacesEachCardOfMultiCardDraw() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.setLibrary(player1, List.of(new IronTuskElephant()));
        harness.setGraveyard(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new InfernalContract()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.GraveyardChoice.class);
            harness.handleGraveyardCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Island"))
                .hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Iron Tusk Elephant"));
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("The card-only graveyard replacement does not exile a token")
    void doesNotExileToken() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Wood");
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, tokenId);

        harness.assertNotOnBattlefield(player1, "Wood");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Wood"));
    }
}
