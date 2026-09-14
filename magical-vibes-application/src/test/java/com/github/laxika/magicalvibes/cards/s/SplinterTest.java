package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Splinter.class, IcyManipulator.class, Plains.class, GrizzlyBears.class})
class SplinterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target artifact and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player2, List.of(new IcyManipulator()));
        harness.setGraveyard(player2, List.of(new IcyManipulator()));

        harness.setLibrary(player2, List.of(new IcyManipulator(), new Plains()));

        harness.setHand(player1, List.of(new Splinter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Icy Manipulator");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Icy Manipulator");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Icy Manipulator"))
                .hasSize(4);
        harness.assertNotInHand(player2, "Icy Manipulator");
        harness.assertNotInGraveyard(player2, "Icy Manipulator");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Icy Manipulator"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Searches only the target artifact controller's zones")
    void searchesOnlyTargetControllerZones() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new IcyManipulator()).getId();
        UUID otherBattlefieldCopyId = harness.addToBattlefieldAndReturn(player2, new IcyManipulator()).getId();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Splinter(), new IcyManipulator()));
        harness.setGraveyard(player1, List.of(new IcyManipulator()));
        harness.setLibrary(player1, List.of(new IcyManipulator()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Icy Manipulator"))
                .extracting(permanent -> permanent.getId())
                .containsExactly(otherBattlefieldCopyId);
        harness.assertInHand(player1, "Icy Manipulator");
        harness.assertInGraveyard(player1, "Icy Manipulator");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Icy Manipulator"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Icy Manipulator"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Icy Manipulator"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Fizzles if the target artifact leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player2, List.of(new IcyManipulator()));
        harness.setHand(player1, List.of(new Splinter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Icy Manipulator");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Icy Manipulator");
        harness.assertInGraveyard(player1, "Splinter");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Splinter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
        .isInstanceOf(IllegalStateException.class);
    }
}
