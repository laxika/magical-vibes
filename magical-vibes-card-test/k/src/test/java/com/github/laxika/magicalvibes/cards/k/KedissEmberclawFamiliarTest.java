package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KedissEmberclawFamiliar.class, GrizzlyBears.class, LoxodonWarhammer.class, Terror.class})
class KedissEmberclawFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when a commander you control deals combat damage")
    void triggersForCommanderCombatDamage() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, commander);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
        });

        assertThat(gameLogContains("Kediss, Emberclaw Familiar's triggered ability goes on the stack."))
                .isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a noncommander creature")
    void doesNotTriggerForNoncommanderCombatDamage() {
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gameLogContains("Kediss, Emberclaw Familiar's triggered ability goes on the stack."))
                .isFalse();
    }

    @Test
    void commanderDealsTheTriggeredDamageToOtherOpponents() {
        Player otherOpponent = addThirdPlayer();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent kediss = addCreatureReady(player1, new KedissEmberclawFamiliar());
        Permanent commander = addCreatureReady(player1, commanderCard);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertLife(otherOpponent, 18);
        assertThat(gd.damageDealtBySourceToPlayerThisTurn(commander.getId(), otherOpponent.getId()))
                .isEqualTo(2);
        assertThat(gd.damageDealtBySourceToPlayerThisTurn(kediss.getId(), otherOpponent.getId()))
                .isZero();
        assertThat(gd.commanderDamageReceived.getOrDefault(otherOpponent.getId(), java.util.Map.of()))
                .isEmpty();
    }

    @Test
    void commandersLifelinkAppliesToTriggeredDamage() {
        Player otherOpponent = addThirdPlayer();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        Permanent commander = addCreatureReady(player1, commanderCard);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        equipment.setAttachedTo(commander.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 15);
        harness.assertLife(otherOpponent, 15);
    }

    @Test
    void triggersForAnOpponentsCommanderUnderYourControl() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, commander);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
        });

        harness.assertLife(player2, 18);
        assertThat(gameLogContains("Kediss, Emberclaw Familiar's triggered ability goes on the stack."))
                .isTrue();
    }

    @Test
    void triggersForKedissItselfWhenItIsACommander() {
        Player otherOpponent = addThirdPlayer();
        Card kediss = new KedissEmberclawFamiliar();
        gd.makeCommander(player1.getId(), kediss);
        addCreatureReady(player1, kediss);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.assertLife(otherOpponent, 19);
    }

    @Test
    void doesNotTriggerForAnOpposingCommander() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player2, commander);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gameLogContains("Kediss, Emberclaw Familiar's triggered ability goes on the stack."))
                .isFalse();
    }

    @Test
    @DisplayName("A commander that left the battlefield deals the damage with the lifelink it had")
    void departedCommanderDealsTheDamageWithItsLifelink() {
        Player otherOpponent = addThirdPlayer();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        Permanent commander = addCreatureReady(player1, commanderCard);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        equipment.setAttachedTo(commander.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
        });
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, commander.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CommanderReturnChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(commander.getId()));
        harness.passBothPriorities();

        harness.assertLife(otherOpponent, 15);
        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("Protection from the commander's color, not Kediss's, prevents the damage")
    void protectionFromTheCommandersColorPreventsTheDamage() {
        Player otherOpponent = addThirdPlayer();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, commanderCard);
        gd.playerProtectionFromColorsUntilEndOfTurn
                .computeIfAbsent(otherOpponent.getId(), k -> new HashSet<>()).add(CardColor.GREEN);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertLife(player2, 18);
        harness.assertLife(otherOpponent, 20);
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }
}
