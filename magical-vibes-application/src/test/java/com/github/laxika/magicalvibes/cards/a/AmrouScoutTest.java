package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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

@CardUsed({AmrouScout.class, DefiantFalcon.class, GrizzlyBears.class, HolyDay.class})
class AmrouScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 3 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyScout();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new DefiantFalcon(),
                new GrizzlyBears(),
                new HolyDay()));

        activateScout();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Defiant Falcon"));
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyScout();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DefiantFalcon());

        activateScout();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Amrou Scout", "Defiant Falcon");
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadyScout();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new HolyDay()));

        activateScout();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Amrou Scout");
    }

    private void addReadyScout() {
        harness.addToBattlefield(player1, new AmrouScout());
        Permanent scout = findPermanent(player1, "Amrou Scout");
        scout.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateScout() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
