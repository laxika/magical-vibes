package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DefiantVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RamosianSkyMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 6 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadySkyMarshal();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new RamosianSkyMarshal(),
                new DefiantVanguard(),
                new GrizzlyBears(),
                new HolyDay()));

        activateSkyMarshal();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Ramosian Sky Marshal", "Defiant Vanguard");
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadySkyMarshal();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DefiantVanguard());

        activateSkyMarshal();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Sky Marshal", "Defiant Vanguard");
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadySkyMarshal();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new HolyDay()));

        activateSkyMarshal();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Sky Marshal");
    }

    private void addReadySkyMarshal() {
        harness.addToBattlefield(player1, new RamosianSkyMarshal());
        Permanent skyMarshal = findPermanent(player1, "Ramosian Sky Marshal");
        skyMarshal.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }

    private void activateSkyMarshal() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
