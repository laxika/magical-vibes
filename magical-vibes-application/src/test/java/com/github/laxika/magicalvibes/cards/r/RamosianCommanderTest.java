package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.d.DefiantVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.l.LinSivviDefiantHero;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RamosianCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability offers Rebel permanents with mana value 5 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyCommander();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new DefiantFalcon(),
                new DefiantVanguard(),
                new LinSivviDefiantHero(),
                new JhovallQueen(),
                new GrizzlyBears(),
                new HolyDay()));

        activateCommander();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Defiant Falcon", "Defiant Vanguard", "Lin Sivvi, Defiant Hero");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyCommander();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DefiantFalcon());

        activateCommander();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Commander", "Defiant Falcon");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addReadyCommander();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new JhovallQueen(), new GrizzlyBears(), new HolyDay()));

        activateCommander();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Commander");
    }

    private void addReadyCommander() {
        harness.addToBattlefield(player1, new RamosianCommander());
        Permanent commander = findPermanent(player1, "Ramosian Commander");
        commander.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void activateCommander() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
