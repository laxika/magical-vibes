package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RysorianBadgerTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new RysorianBadger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void setDefenderGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant(), new Forest()));
    }

    private void attackUnblocked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, which asks for its graveyard targets.
        harness.passBothPriorities();
    }

    private List<UUID> validChoiceIds() {
        return new ArrayList<>(gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
    }

    private List<String> defenderGraveyardNames() {
        return gd.playerGraveyards.get(player2.getId()).stream().map(Card::getName).toList();
    }

    private void advanceThroughCombatDamage() {
        for (int i = 0; i < 6; i++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Only creature cards in the defending player's graveyard can be chosen")
    void onlyDefendersCreatureCardsAreChoosable() {
        setDefenderGraveyard();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addAttacker();

        attackUnblocked();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        List<UUID> defenderCreatureIds = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> !card.getName().equals("Forest"))
                .map(Card::getId)
                .toList();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(defenderCreatureIds);
    }

    @Test
    @DisplayName("Exiling two creature cards gains 2 life and the badger deals no combat damage")
    void exilingTwoGainsLifeAndPreventsCombatDamage() {
        setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, validChoiceIds());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(defenderGraveyardNames()).containsExactly("Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);

        advanceThroughCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exiling a single creature card gains only 1 life")
    void exilingOneGainsOneLife() {
        setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, List.of(validChoiceIds().getFirst()));
        harness.passBothPriorities();

        assertThat(defenderGraveyardNames()).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Choosing no cards gains no life and the badger still deals combat damage")
    void choosingNoCardsLeavesCombatDamageIntact() {
        setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(defenderGraveyardNames()).containsExactly("Grizzly Bears", "Hill Giant", "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        advanceThroughCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No creature card in the defending player's graveyard presents no choice")
    void noCreatureCardsPresentsNoChoice() {
        harness.setGraveyard(player2, List.of(new Forest()));
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(defenderGraveyardNames()).containsExactly("Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        advanceThroughCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A blocked badger does not trigger at all")
    void blockedBadgerDoesNotTrigger() {
        setDefenderGraveyard();

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(defenderGraveyardNames()).containsExactly("Grizzly Bears", "Hill Giant", "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
