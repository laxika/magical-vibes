package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavingGrace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a prompt addressed to one player says about face-down permanents: the player who controls one may look at it
 * (CR 708.5) and must be able to tell it from their others (CR 708.6), so their prompt names it; another player's
 * prompt, the log and everything else keeps it masked.
 */
@CardUsed({AinokSurvivalist.class, ExaltedAngel.class, GrizzlyBears.class, SavingGrace.class, Shock.class})
class FaceDownControllerPromptTest extends BaseCardTest {

    private static final String MASKED = "a face-down permanent";

    private final ObjectMapper json = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("A replacement prompt names each face-down permanent of the player it asks")
    void replacementOrderNamesOwnFaceDownPermanents() {
        Permanent angel = faceDown(player1, new ExaltedAngel());
        Permanent survivalist = faceDown(player1, new AinokSurvivalist());
        enchantWithSavingGrace(angel);
        enchantWithSavingGrace(survivalist);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.clearMessages();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Redirect the damage to Exalted Angel", "Redirect the damage to Ainok Survivalist");
        assertThat(harness.getConn1().getMessagesContaining("Redirect the damage to Ainok Survivalist")).isNotEmpty();

        harness.handleListChoice(player1, "Redirect the damage to Ainok Survivalist");

        harness.assertLife(player1, 20);
        assertThat(survivalist.getMarkedDamage()).isEqualTo(2);
        assertThat(angel.getMarkedDamage()).isZero();
        assertHiddenFrom(player2, angel.getCard(), survivalist.getCard());
    }

    @Test
    @DisplayName("A combat damage assignment names the face-down attacker of the player assigning it")
    void damageAssignmentNamesOwnFaceDownAttacker() {
        Permanent angel = faceDown(player1, new ExaltedAngel());
        Permanent survivalist = faceDown(player1, new AinokSurvivalist());
        attack(angel, player2);
        attack(survivalist, player2);
        blockWithTwoBears(player2, player1, 0);
        blockWithTwoBears(player2, player1, 1);

        resolveCombat(player1);

        PendingInteraction.CombatDamageAssignment first = assignment();
        assertThat(first.playerId()).isEqualTo(player1.getId());
        assertThat(latestAssignment(player1).get("attackerName").asString()).isEqualTo("Exalted Angel");
        assertThat(first.attackerName()).isEqualTo(MASKED);

        harness.handleCombatDamageAssigned(player1, first.attackerIndex(),
                Map.of(first.validTargets().getFirst().id(), 2));

        PendingInteraction.CombatDamageAssignment second = assignment();
        assertThat(second.attackerPermanentId()).isEqualTo(survivalist.getId());
        assertThat(latestAssignment(player1).get("attackerName").asString()).isEqualTo("Ainok Survivalist");
        assertHiddenFrom(player2, angel.getCard(), survivalist.getCard());
    }

    @Test
    @DisplayName("Another player's combat damage assignment keeps face-down blockers masked")
    void opponentsAssignmentKeepsFaceDownBlockersMasked() {
        Permanent angel = faceDown(player1, new ExaltedAngel());
        Permanent survivalist = faceDown(player1, new AinokSurvivalist());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attack(attacker, player1);
        for (Permanent blocker : List.of(angel, survivalist)) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blocker.addBlockingTargetId(attacker.getId());
        }

        resolveCombat(player2);

        assertThat(assignment().playerId()).isEqualTo(player2.getId());
        JsonNode prompt = latestAssignment(player2);
        assertThat(prompt.get("validTargets")).extracting(target -> target.get("name").asString())
                .containsExactly(MASKED, MASKED);
        assertHiddenFrom(player2, angel.getCard(), survivalist.getCard());
    }

    @Test
    @DisplayName("A face-down permanent is named to the player who gained control of it, not to its owner")
    void controllerNotOwnerIsTold() {
        Permanent angel = faceDown(player1, new ExaltedAngel());
        gd.playerBattlefields.get(player1.getId()).remove(angel);
        gd.playerBattlefields.get(player2.getId()).add(angel);
        attack(angel, player1);
        blockWithTwoBears(player1, player2, 0);

        resolveCombat(player2);

        assertThat(assignment().playerId()).isEqualTo(player2.getId());
        assertThat(latestAssignment(player2).get("attackerName").asString()).isEqualTo("Exalted Angel");
        assertThat(FaceDownVisibility.nameShownTo(gd, angel, player1.getId())).isEqualTo(MASKED);
        assertHiddenFrom(player1, angel.getCard());
    }

    private Permanent faceDown(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }

    private void enchantWithSavingGrace(Permanent creature) {
        harness.setHand(player1, List.of(new SavingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void attack(Permanent attacker, Player defender) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
    }

    /** Two of {@code blockingPlayer}'s Grizzly Bears block the attacker at {@code attackerIndex}. */
    private void blockWithTwoBears(Player blockingPlayer, Player attackingPlayer, int attackerIndex) {
        Permanent attacker = gd.playerBattlefields.get(attackingPlayer.getId()).get(attackerIndex);
        for (int i = 0; i < 2; i++) {
            Permanent blocker = addCreatureReady(blockingPlayer, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(attackerIndex);
            blocker.addBlockingTargetId(attacker.getId());
        }
    }

    private PendingInteraction.CombatDamageAssignment assignment() {
        PendingInteraction.CombatDamageAssignment assignment =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(assignment).isNotNull();
        return assignment;
    }

    private JsonNode latestAssignment(Player viewer) {
        List<String> messages = connectionOf(viewer).getMessagesContaining("COMBAT_DAMAGE_ASSIGNMENT");
        assertThat(messages).isNotEmpty();
        return json.readTree(messages.getLast());
    }

    private FakeConnection connectionOf(Player player) {
        return player == player1 ? harness.getConn1() : harness.getConn2();
    }

    /** Nothing {@code viewer} is sent, and nothing in the log, names any of {@code cards}. */
    private void assertHiddenFrom(Player viewer, Card... cards) {
        harness.publishState();
        List<String> log = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        for (Card card : cards) {
            assertThat(log).noneMatch(line -> line.contains(card.getName()));
            assertThat(connectionOf(viewer).getSentMessages()).isNotEmpty()
                    .noneMatch(message -> message.contains(card.getName()));
        }
    }
}
