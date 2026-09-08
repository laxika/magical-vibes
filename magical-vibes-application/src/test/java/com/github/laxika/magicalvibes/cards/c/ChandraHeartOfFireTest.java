package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandraHeartOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("+1 discards the hand and grants permission to play the top three cards")
    void plusOneDiscardsHandAndExilesTopThreeForPlay() {
        Permanent chandra = addReadyChandra(player1, 3);
        Card first = new Forest();
        Card second = new Shock();
        Card third = new Opt();
        Card discardedOne = new Forest();
        Card discardedTwo = new Opt();
        harness.setHand(player1, List.of(discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("+1 deals 2 damage to a target player")
    void plusOneDealsTwoDamage() {
        Permanent chandra = addReadyChandra(player1, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("−9 exiles chosen red spells from the graveyard and library and adds six red mana")
    void minusNineExilesChosenRedSpellsAndAddsMana() {
        Permanent chandra = addReadyChandra(player1, 9);
        Card graveyardRed = new Shock();
        Card graveyardBlue = new Opt();
        Card libraryRed = new Shock();
        Card libraryBlue = new Opt();
        harness.setGraveyard(player1, List.of(graveyardRed, graveyardBlue));
        harness.setLibrary(player1, List.of(libraryRed, libraryBlue));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardRed.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(graveyardRed.getId(), libraryRed.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(graveyardRed.getId(), player1.getId())
                .containsEntry(libraryRed.getId(), player1.getId())
                .doesNotContainKey(graveyardBlue.getId())
                .doesNotContainKey(libraryBlue.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(graveyardBlue)
                .doesNotContain(graveyardRed, libraryRed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryBlue);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ChandraHeartOfFire());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
