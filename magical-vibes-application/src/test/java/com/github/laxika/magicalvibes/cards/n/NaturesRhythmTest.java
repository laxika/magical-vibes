package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

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
}
