package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReachOfBranchesTest extends BaseCardTest {

    private void prepareMain(com.github.laxika.magicalvibes.model.Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting creates a 2/5 Treefolk Shaman token")
    void createsToken() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new ReachOfBranches()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4); // {4}{G}

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treefolk Shaman"))
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Accepting the Forest trigger returns Reach of Branches from graveyard to hand")
    void forestReturnsFromGraveyardOnAccept() {
        ReachOfBranches reach = new ReachOfBranches();
        harness.setGraveyard(player1, List.of(reach));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0); // play the Forest
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(reach.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(reach.getId()));
    }

    @Test
    @DisplayName("Declining the Forest trigger keeps Reach of Branches in the graveyard")
    void forestDeclineKeepsInGraveyard() {
        ReachOfBranches reach = new ReachOfBranches();
        harness.setGraveyard(player1, List.of(reach));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(reach.getId()));
    }

    @Test
    @DisplayName("A non-Forest land entering does not trigger")
    void nonForestLandDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new ReachOfBranches()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Island()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Forest an opponent controls entering does not trigger")
    void opponentForestDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new ReachOfBranches()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).isEmpty();
    }
}
