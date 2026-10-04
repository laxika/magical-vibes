package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulcallersBell.class, Swamp.class})
class GhoulcallersBellTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Ghoulcaller's Bell")
    void activatingTapsBell() {
        Permanent bell = addReadyBell(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bell.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent bell = addReadyBell(player1);
        bell.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Controller mills a card when ability resolves")
    void controllerMillsCard() {
        addReadyBell(player1);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        int deckSizeBefore = deck.size();
        Card topCard = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Opponent mills a card when ability resolves")
    void opponentMillsCard() {
        addReadyBell(player1);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        int deckSizeBefore = deck.size();
        Card topCard = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Both players mill simultaneously")
    void bothPlayersMill() {
        addReadyBell(player1);

        int p1DeckBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        int p2DeckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not crash when controller library is empty")
    void doesNotCrashWhenControllerLibraryEmpty() {
        addReadyBell(player1);
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        // Opponent should still mill
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not crash when opponent library is empty")
    void doesNotCrashWhenOpponentLibraryEmpty() {
        addReadyBell(player1);
        harness.getGameData().playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // Controller should still mill
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A newly entered noncreature Bell can activate without mana and mills only on resolution")
    void newlyEnteredBellUsesStack() {
        Permanent bell = harness.enterBattlefieldAndReturn(player1, new GhoulcallersBell());
        Swamp controllerTop = new Swamp();
        Swamp opponentTop = new Swamp();
        harness.setLibrary(player1, List.of(controllerTop));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.activateAbility(player1, 0, null, null);

        assertThat(bell.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(controllerTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    @DisplayName("Ability still mills both players after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent bell = addReadyBell(player1);
        Swamp controllerTop = new Swamp();
        Swamp opponentTop = new Swamp();
        harness.setLibrary(player1, List.of(controllerTop));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(bell);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(controllerTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBell(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GhoulcallersBell());
        perm.setSummoningSick(false);
        return perm;
    }
}
