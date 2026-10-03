package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NarsetsReversal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArrestersAdmonition.class, AxebaneBeast.class, AzoriusLocket.class, NarsetsReversal.class})
class ArrestersAdmonitionTest extends BaseCardTest {

    @Test
    void bouncesCreatureAndDrawsDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast()).getId();
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Axebane Beast");
        harness.assertInHand(player2, "Axebane Beast");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void bouncesCreatureWithoutDrawingOutsideMainPhase() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast()).getId();
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Axebane Beast");
        harness.assertInHand(player2, "Axebane Beast");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    void cannotTargetNonCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AzoriusLocket()).getId();
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void drawsDuringPostcombatMainPhase() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast()).getId();
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.setLibrary(player1, List.of(new AzoriusLocket()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInHand(player2, "Axebane Beast");
        harness.assertInHand(player1, "Azorius Locket");
        harness.assertInGraveyard(player1, "Arrester's Admonition");
    }

    @Test
    void doesNotDrawDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast()).getId();
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.setLibrary(player1, List.of(new AzoriusLocket()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInHand(player2, "Axebane Beast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenOnlyTargetLeavesBeforeResolution() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast()).getId();
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.setHand(player2, List.of(new ArrestersAdmonition()));
        harness.setLibrary(player1, List.of(new AzoriusLocket()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Axebane Beast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Arrester's Admonition");
    }

    @Test
    void copyDuringOwnMainPhaseDoesNotReceiveAddendumBonus() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AxebaneBeast()).getId();
        ArrestersAdmonition admonition = new ArrestersAdmonition();
        harness.setHand(player1, List.of(admonition, new NarsetsReversal()));
        harness.setLibrary(player1, List.of(new AzoriusLocket()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, admonition.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Axebane Beast");
        harness.assertInHand(player1, "Arrester's Admonition");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
