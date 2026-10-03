package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.OrochiMergeKeeper;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DocksideChef.class, OrochiMergeKeeper.class, NetworkTerminal.class})
class DocksideChefTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Dockside Chef itself draws a card")
    void canSacrificeItselfAndDraw() {
        harness.addToBattlefield(player1, new DocksideChef());
        addActivationMana();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dockside Chef");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing an artifact or creature draws a card")
    void choosesArtifactOrCreatureToSacrifice() {
        Permanent chef = harness.addToBattlefieldAndReturn(player1, new DocksideChef());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrochiMergeKeeper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chef.getId(), creature.getId(), artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Network Terminal");
        harness.assertOnBattlefield(player1, "Dockside Chef");
        harness.assertOnBattlefield(player1, "Orochi Merge-Keeper");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Chef can sacrifice a creature; the draw waits for resolution")
    void sacrificesCreatureAsCostAndDrawsOnResolution() {
        Permanent chef = harness.addToBattlefieldAndReturn(player1, new DocksideChef());
        chef.tap();
        chef.setSummoningSick(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrochiMergeKeeper());
        harness.addToBattlefield(player2, new NetworkTerminal());
        harness.addToBattlefield(player2, new OrochiMergeKeeper());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NetworkTerminal(), new DocksideChef()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chef.getId(), creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertInGraveyard(player1, "Orochi Merge-Keeper");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Network Terminal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        harness.assertOnBattlefield(player1, "Dockside Chef");
        harness.assertOnBattlefield(player2, "Orochi Merge-Keeper");
        harness.assertOnBattlefield(player2, "Network Terminal");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
