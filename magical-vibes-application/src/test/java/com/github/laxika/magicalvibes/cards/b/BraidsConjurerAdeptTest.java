package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DawnCharm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidsConjurerAdept.class, DawnCharm.class, FountainOfYouth.class,
        GossamerPhantasm.class, UrborgTombOfYawgmoth.class})
class BraidsConjurerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may put an artifact, creature, or land from hand onto the battlefield")
    void activePlayerMayPutEligibleCardFromHandOntoBattlefield() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card artifact = new FountainOfYouth();
        Card creature = new GossamerPhantasm();
        Card land = new UrborgTombOfYawgmoth();
        Card instant = new DawnCharm();
        harness.setHand(player2, List.of(artifact, creature, land, instant));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0, 1, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(artifact, land, instant);
        assertThat(findPermanent(player2, "Gossamer Phantasm").getCard()).isSameAs(creature);
    }

    @Test
    @DisplayName("Declining the upkeep choice leaves the active player's hand unchanged")
    void decliningLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card creature = new GossamerPhantasm();
        harness.setHand(player2, List.of(creature));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player2, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Braids also offers its controller a creature on their own upkeep without paying mana")
    void controllerMayPutCreatureOnOwnUpkeep() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card creature = new GossamerPhantasm();
        Card opponentCreature = new GossamerPhantasm();
        harness.setHand(player1, List.of(creature));
        harness.setHand(player2, List.of(opponentCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature);
        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertNotOnBattlefield(player2, "Gossamer Phantasm");
        assertThat(findPermanent(player1, "Gossamer Phantasm").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Gossamer Phantasm").isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The opponent may put exactly one artifact onto their battlefield untapped")
    void opponentMayPutArtifact() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card artifact = new FountainOfYouth();
        Card creature = new GossamerPhantasm();
        harness.setHand(player2, List.of(artifact, creature));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        assertThat(findPermanent(player2, "Fountain of Youth").isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Putting a land onto the battlefield during upkeep does not use a land play")
    void opponentMayPutLandWithoutUsingLandPlay() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card land = new UrborgTombOfYawgmoth();
        harness.setHand(player2, List.of(land));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertNotOnBattlefield(player1, "Urborg, Tomb of Yawgmoth");
        assertThat(findPermanent(player2, "Urborg, Tomb of Yawgmoth").isTapped()).isFalse();
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting with no eligible card leaves the hand unchanged and completes resolution")
    void acceptingWithNoEligibleCardCompletesResolution() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card instant = new DawnCharm();
        harness.setHand(player2, List.of(instant));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability resolves even after Braids leaves the battlefield")
    void triggerResolvesAfterBraidsLeaves() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card creature = new GossamerPhantasm();
        harness.setHand(player2, List.of(creature));

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Card braids = gd.playerBattlefields.get(player1.getId()).removeFirst().getCard();
        harness.setGraveyard(player1, List.of(braids));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Braids, Conjurer Adept");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
