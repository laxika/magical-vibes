package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyreOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and finds a same-type creature with mana value one higher")
    void sacrificesCreatureAndSearchesForMatchingCreature() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new ElvishVisionary(),
                new GrizzlyBears(),
                new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(1)
                .allMatch(card -> card.getName().equals("Elvish Visionary"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Elvish Visionary");
    }

    @Test
    @DisplayName("Does not find a creature with the wrong type or mana value")
    void doesNotFindNonMatchingCreature() {
        harness.addToBattlefield(player1, new PyreOfHeroes());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }
}
