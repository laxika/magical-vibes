package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RamosianLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 3 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyLieutenant();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new DefiantFalcon(),
                new RamosianCommander(),
                new GrizzlyBears(),
                new HolyDay()));

        activateLieutenant();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Defiant Falcon");
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyLieutenant();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DefiantFalcon());

        activateLieutenant();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Lieutenant", "Defiant Falcon");
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadyLieutenant();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new HolyDay()));

        activateLieutenant();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Lieutenant");
    }

    private void addReadyLieutenant() {
        harness.addToBattlefield(player1, new RamosianLieutenant());
        Permanent lieutenant = findPermanent(player1, "Ramosian Lieutenant");
        lieutenant.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateLieutenant() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
