package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CranialArchive.class, AlpineGrizzly.class, WetlandSambar.class})
class CranialArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself, shuffles the target graveyard, and draws a card")
    void exilesSelfShufflesTargetGraveyardAndDraws() {
        harness.addToBattlefield(player1, new CranialArchive());
        harness.setGraveyard(player2, List.of(new AlpineGrizzly(), new WetlandSambar()));
        harness.setGraveyard(player1, List.of(new AlpineGrizzly()));
        harness.setLibrary(player1, List.of(new WetlandSambar()));
        harness.setHand(player1, List.of());
        int targetLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetLibrarySize + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Cranial Archive");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cranial Archive"));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new CranialArchive());
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Alpine Grizzly")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its controller and draw from the shuffled graveyard")
    void targetsSelfAndShufflesBeforeDrawing() {
        harness.addToBattlefield(player1, new CranialArchive());
        AlpineGrizzly card = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(card));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Cranial Archive"));
    }

    @Test
    @DisplayName("An empty target graveyard still allows the controller to draw")
    void emptyTargetGraveyardStillDraws() {
        harness.addToBattlefield(player1, new CranialArchive());
        harness.setGraveyard(player2, List.of());
        WetlandSambar card = new WetlandSambar();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        int targetLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetLibrarySize);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exile is paid before the ability resolves, even when the artifact is tapped")
    void exilesAsCostBeforeResolutionWithoutTapping() {
        harness.addToBattlefieldAndReturn(player1, new CranialArchive()).tap();
        AlpineGrizzly card = new AlpineGrizzly();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WetlandSambar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Cranial Archive");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Cranial Archive"));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Wetland Sambar");
    }
}
