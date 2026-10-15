package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturesRhythm.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Plains.class})
class NaturesRhythmTest extends BaseCardTest {

    @Test
    void searchesForACreatureWithManaValueAtMostXAndPutsItOntoTheBattlefield() {
        harness.setHand(player1, List.of(new NaturesRhythm()));
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Llanowar Elves")
                        || permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void harmonizeUsesTheChosenXAndReducesItsGenericCostByTappedCreaturePower() {
        Card spell = new NaturesRhythm();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new LlanowarElves(), new HillGiant(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.getGameService().playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(), null,
                List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    @Test
    void canFailToFindEvenWhenAnEligibleCreatureExists() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new NaturesRhythm()));
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Nature's Rhythm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zeroXDoesNotFindCreaturesWithPositiveManaValue() {
        Card creature = new LlanowarElves();
        harness.setHand(player1, List.of(new NaturesRhythm()));
        harness.setLibrary(player1, List.of(creature, new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(creature).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Nature's Rhythm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void harmonizeCanBeCastWithoutTappingACreatureAndExilesAfterTheSearch() {
        Card spell = new NaturesRhythm();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 6);

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(), null, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Nature's Rhythm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void harmonizePowerGreaterThanXLeavesTheColoredCostAndChosenXUnchanged() {
        Card spell = new NaturesRhythm();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(), null,
                List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }
}
