package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrongboxRaider.class, Forest.class})
class StrongboxRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Raid exiles the top two cards and lets you choose one to play until your next turn")
    void raidExilesTwoCardsAndGrantsChosenCardPermission() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card chosen = new Forest();
        Card other = new Forest();
        harness.setLibrary(player1, List.of(chosen, other));

        castStrongboxRaider();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, other);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(other.getId());
    }

    @Test
    @DisplayName("Raid does not exile cards when you did not attack this turn")
    void withoutRaidDoesNothing() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        castStrongboxRaider();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentAttackingDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        castStrongboxRaider();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void raidWithEmptyLibraryDoesNotRequestAChoice() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.setLibrary(player1, List.of());

        castStrongboxRaider();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void raidWithOneCardStillRequiresChoosingItAndAllowsPlayingALand() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castStrongboxRaider();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.castFromExile(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(land.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void unchosenCardCannotBePlayed() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card chosen = new Forest();
        Card other = new Forest();
        harness.setLibrary(player1, List.of(chosen, other));

        castStrongboxRaider();
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, other);
    }

    private void castStrongboxRaider() {
        harness.setHand(player1, List.of(new StrongboxRaider()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
