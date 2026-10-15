package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindsOfRebuke.class, DuneBeetle.class, Island.class})
class WindsOfRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Bounces target nonland permanent and mills each player two cards")
    void bouncesAndMills() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addToBattlefield(player2, new DuneBeetle());
        UUID targetId = harness.getPermanentId(player2, "Dune Beetle");
        harness.setHand(player1, List.of(new WindsOfRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        harness.assertInHand(player2, "Dune Beetle");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new DuneBeetle()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new WindsOfRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Does not mill either player when the only target leaves before resolution")
    void illegalTargetPreventsMilling() {
        harness.addToBattlefield(player2, new DuneBeetle());
        UUID targetId = harness.getPermanentId(player2, "Dune Beetle");
        harness.setHand(player1, List.of(new WindsOfRebuke(), new WindsOfRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Dune Beetle");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 2);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can bounce an own permanent and mills only the available library cards")
    void ownPermanentAndShortLibraries() {
        harness.addToBattlefield(player1, new DuneBeetle());
        UUID targetId = harness.getPermanentId(player1, "Dune Beetle");
        Island remainingCard = new Island();
        harness.setLibrary(player1, List.of(remainingCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new WindsOfRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        harness.assertInHand(player1, "Dune Beetle");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(remainingCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
