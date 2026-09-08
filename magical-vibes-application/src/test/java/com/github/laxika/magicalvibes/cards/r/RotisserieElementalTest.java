package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RotisserieElemental.class)
class RotisserieElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage adds a skewer counter and offers the sacrifice")
    void combatDamageAddsCounterAndOffersSacrifice() {
        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.SKEWER)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Sacrificing exiles cards equal to the updated skewer counter count")
    void sacrificeExilesCardsEqualToCounterCount() {
        Card first = libraryCard("First");
        Card second = libraryCard("Second");
        Card third = libraryCard("Third");
        harness.setLibrary(player1, List.of(first, second, third));

        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.SKEWER, 2);
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(elemental.getCounterCount(CounterType.SKEWER)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the elemental and library unchanged")
    void decliningSacrificeDoesNothingAfterAddingCounter() {
        Card first = libraryCard("First");
        harness.setLibrary(player1, List.of(first));

        Permanent elemental = addReadyElemental();
        elemental.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(elemental.getCounterCount(CounterType.SKEWER)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first);
    }

    private Permanent addReadyElemental() {
        Permanent elemental = new Permanent(new RotisserieElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elemental);
        return elemental;
    }

    private Card libraryCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
