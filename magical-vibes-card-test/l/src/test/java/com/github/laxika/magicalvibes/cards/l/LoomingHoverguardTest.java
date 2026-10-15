package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoomingHoverguard.class, Ornithopter.class, Island.class})
class LoomingHoverguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts target artifact on top of its owner's library")
    void etbPutsTargetArtifactOnTopOfOwnersLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Ornithopter");
        harness.assertOnBattlefield(player1, "Looming Hoverguard");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB fizzles if the target artifact leaves before resolution")
    void etbFizzlesIfTargetArtifactLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("ETB can put an artifact you control on top of your library")
    void etbCanTargetOwnArtifact() {
        Ornithopter artifact = new Ornithopter();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, artifact).getId();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(artifact);
    }

    @Test
    @DisplayName("A controlled artifact goes to its owner's library, not its controller's")
    void controlledArtifactReturnsToOwnersLibrary() {
        Ornithopter artifact = new Ornithopter();
        artifact.setOwnerId(player2.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, artifact).getId();
        int controllerDeckSize = gd.playerDecks.get(player1.getId()).size();
        int ownerDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerDeckSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(ownerDeckSize + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(artifact);
    }

    @Test
    @DisplayName("Hoverguard can enter when no artifacts are available to target")
    void entersWithoutAvailableArtifactTargets() {
        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Looming Hoverguard");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability resolves after Hoverguard leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        Ornithopter artifact = new Ornithopter();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, artifact).getId();
        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent hoverguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LoomingHoverguard)
                .findFirst().orElseThrow();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hoverguard));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Looming Hoverguard");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(artifact);
    }
}
