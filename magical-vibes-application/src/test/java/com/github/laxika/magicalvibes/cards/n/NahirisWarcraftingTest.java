package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SiegeMastodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahirisWarcrafting.class, HillGiant.class, SiegeMastodon.class, Forest.class, Shock.class})
class NahirisWarcraftingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one of the top cards equal to the excess damage and bottoms the rest")
    void exilesOneCardAndBottomsTheRest() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest1 = new Forest();
        Card shock = new Shock();
        Card forest2 = new Forest();
        setLibrary(List.of(forest1, shock, forest2));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest1, shock, forest2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest1, forest2);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Does not look at cards when the damage is not excess")
    void noExcessDamageDoesNotLookAtCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeMastodon());
        Card forest = new Forest();
        Card shock = new Shock();
        setLibrary(List.of(forest, shock));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline to exile a looked-at card")
    void mayDecline() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest = new Forest();
        Card shock = new Shock();
        setLibrary(List.of(forest, shock));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(forest.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        setLibrary(List.of(new Forest()));
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new NahirisWarcrafting()));
        harness.addMana(player1, ManaColor.RED, 3);
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
