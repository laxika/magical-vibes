package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VizierOfDefermentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that attacked this turn")
    void exilesCreatureThatAttackedThisTurn() {
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setAttacking(true); // attacked this turn
        UUID attackerId = attacker.getId();

        castVizierToMayPrompt(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attackerId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles a creature that blocked this turn — status survives into the postcombat main phase")
    void exilesCreatureThatBlockedThisTurn() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new GiantSpider()); // blocker
        declareBlock();

        UUID blockerId = harness.getPermanentId(player2, "Giant Spider");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        castVizierToMayPrompt(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blockerId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Exiled creature returns under its owner's control at the next end step")
    void exiledCreatureReturnsAtNextEndStep() {
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        UUID attackerId = attacker.getId();

        castVizierToMayPrompt(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attackerId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature that neither attacked nor blocked is not a legal target — ETB never triggers")
    void cannotTargetCreatureThatDidNotAttackOrBlock() {
        addReady(player2, new GrizzlyBears()); // never attacked or blocked

        harness.setHand(player1, List.of(new VizierOfDeferment()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // creature enters; ETB finds no legal target

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature on the battlefield")
    void decliningMayDoesNotExile() {
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        castVizierToMayPrompt(player1);
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castVizierToMayPrompt(Player caster) {
        harness.setHand(caster, List.of(new VizierOfDeferment()));
        harness.addMana(caster, ManaColor.WHITE, 3);
        harness.castCreature(caster, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
    }

    private void declareBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
