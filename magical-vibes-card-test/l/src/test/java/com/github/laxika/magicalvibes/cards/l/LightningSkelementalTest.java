package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSkelemental.class, GrizzlyBears.class, Forest.class})
class LightningSkelementalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard two cards")
    void combatDamageMakesDamagedPlayerDiscardTwoCards() {
        Permanent skelemental = addAttackingSkelemental(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skelemental);
    }

    @Test
    @DisplayName("Lightning Skelemental is sacrificed at the end step")
    void sacrificedAtEndStep() {
        harness.addToBattlefieldAndReturn(player1, new LightningSkelemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lightning Skelemental");
        harness.assertInGraveyard(player1, "Lightning Skelemental");
    }

    @Test
    @DisplayName("The damaged player chooses exactly two cards from a larger hand")
    void damagedPlayerChoosesTwoCards() {
        addAttackingSkelemental(player1);
        Forest retained = new Forest();
        GrizzlyBears firstDiscard = new GrizzlyBears();
        Forest secondDiscard = new Forest();
        Forest controllerCard = new Forest();
        harness.setHand(player1, List.of(controllerCard));
        harness.setHand(player2, List.of(retained, firstDiscard, secondDiscard));

        resolveCombat();
        resolveAllTriggers();
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
    }

    @Test
    @DisplayName("A player with one card discards it and finishes resolving the trigger")
    void damagedPlayerWithOneCardDiscardsIt() {
        addAttackingSkelemental(player1);
        Forest card = new Forest();
        harness.setHand(player2, List.of(card));

        resolveCombat();
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player with an empty hand resolves without a discard prompt")
    void damagedPlayerWithEmptyHandDoesNotChooseCards() {
        addAttackingSkelemental(player1);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample damage triggers discard even when Lightning Skelemental dies in combat")
    void trampleDamageTriggersDiscardAfterSourceDies() {
        addAttackingSkelemental(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 4));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Lightning Skelemental");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Haste allows Lightning Skelemental to attack while summoning sick")
    void canAttackImmediately() {
        Permanent skelemental = harness.addToBattlefieldAndReturn(player1, new LightningSkelemental());
        skelemental.setSummoningSick(true);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Assigning all trample damage to a blocker does not trigger discard")
    void noDiscardWithoutDamageToPlayer() {
        addAttackingSkelemental(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Forest card = new Forest();
        harness.setHand(player2, List.of(card));
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Lightning Skelemental");
    }

    @Test
    @DisplayName("Lightning Skelemental is sacrificed during an opponent's end step")
    void sacrificedAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new LightningSkelemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lightning Skelemental");
        harness.assertInGraveyard(player1, "Lightning Skelemental");
    }

    private Permanent addAttackingSkelemental(Player player) {
        Permanent skelemental = addCreatureReady(player, new LightningSkelemental());
        skelemental.setAttacking(true);
        return skelemental;
    }
}
