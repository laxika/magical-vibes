package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WistfulnessTest extends BaseCardTest {

    @Test
    void twoGreenManaExilesAnOpponentsArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticStar());
        harness.setHand(player1, List.of(new Wistfulness()));
        addRegularMana(ManaColor.GREEN);

        harness.castCreature(player1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chromatic Star");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Chromatic Star");
    }

    @Test
    void twoBlueManaDrawsTwoThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new Wistfulness(), new GrizzlyBears()));
        addRegularMana(ManaColor.BLUE);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void oneManaOfEachColorTriggersNeitherBranch() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticStar());
        harness.setHand(player1, List.of(new Wistfulness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Chromatic Star");
        harness.assertOnBattlefield(player1, "Wistfulness");
    }

    @Test
    void evokeWithTwoBlueManaDrawsAndSacrificesWistfulness() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new Wistfulness(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Wistfulness");
        harness.assertInGraveyard(player1, "Wistfulness");
    }

    @Test
    void greenBranchCannotTargetACreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Wistfulness()));
        addRegularMana(ManaColor.GREEN);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void greenBranchCannotTargetAnArtifactYouControl() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new ChromaticStar()).getId();
        harness.setHand(player1, List.of(new Wistfulness()));
        addRegularMana(ManaColor.GREEN);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addRegularMana(ManaColor coloredMana) {
        harness.addMana(player1, coloredMana, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
