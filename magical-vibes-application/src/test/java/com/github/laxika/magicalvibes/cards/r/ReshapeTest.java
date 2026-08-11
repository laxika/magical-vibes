package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReshapeTest extends BaseCardTest {

    @Test
    @DisplayName("Reshape sacrifices an artifact and searches for an artifact with mana value X or less")
    void sacrificesArtifactAndFindsMatchingArtifact() {
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        MyrRetriever foundArtifact = new MyrRetriever();
        Ornithopter tooSmallArtifact = new Ornithopter();
        DarksteelIngot tooExpensiveArtifact = new DarksteelIngot();
        GrizzlyBears nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(foundArtifact, tooSmallArtifact, tooExpensiveArtifact, nonArtifact));
        harness.setHand(player1, List.of(new Reshape()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false, sacrificedArtifact.getId());

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(foundArtifact, tooSmallArtifact);

        int foundIndex = search.params().cards().indexOf(foundArtifact);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(foundIndex));

        harness.assertOnBattlefield(player1, "Myr Retriever");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reshape cannot be cast without an artifact to sacrifice")
    void requiresArtifactSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Reshape()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Reshape");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
