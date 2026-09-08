package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VelomachusLoreholdTest extends BaseCardTest {

    @Test
    void offersOnlyMatchingSpellsWithinItsPower() {
        Permanent velomachus = addCreatureReady(player1, new VelomachusLorehold());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Pyroclasm pyroclasm = new Pyroclasm();
        BlasphemousAct blasphemousAct = new BlasphemousAct();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(pyroclasm, blasphemousAct, forest, bears));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Pyroclasm");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(velomachus.isTapped()).isFalse();
    }

    @Test
    void castsTheChosenSpellWithoutPayingAndBottomsTheRest() {
        addCreatureReady(player1, new VelomachusLorehold());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Pyroclasm pyroclasm = new Pyroclasm();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(pyroclasm, forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pyroclasm");
    }

    @Test
    void decliningTheSpellBottomsAllLookedAtCards() {
        addCreatureReady(player1, new VelomachusLorehold());
        Pyroclasm pyroclasm = new Pyroclasm();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(pyroclasm, forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(pyroclasm, forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(pyroclasm);
    }
}
