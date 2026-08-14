package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemporalApertureTest extends BaseCardTest {

    private void setDeck(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void activate() {
        harness.addToBattlefield(player1, new TemporalAperture());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Activation reveals the top card and grants delayed free play")
    void activationGrantsFreePlayWithoutImmediateChoice() {
        Shock shock = new Shock();
        setDeck(shock);

        activate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .containsEntry(player1.getId(), shock.getId());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("The revealed spell can be cast later without paying mana")
    void castsRevealedSpellLaterForFree() {
        Shock shock = new Shock();
        setDeck(shock);
        activate();

        harness.castFromLibraryTop(player1, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The permission does not follow a card that leaves the top")
    void permissionEndsWhenCardLeavesTop() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        setDeck(shock);
        activate();

        gd.playerDecks.get(player1.getId()).addFirst(forest);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The revealed land can be played from the top")
    void playsRevealedLandFromTop() {
        Forest forest = new Forest();
        setDeck(forest);
        activate();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The free-play permission expires at cleanup")
    void permissionExpiresAtCleanup() {
        Shock shock = new Shock();
        setDeck(shock);
        activate();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .doesNotContainKey(player1.getId());
    }
}
