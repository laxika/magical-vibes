package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VizierOfDeferment.class, PouncingCheetah.class, GiantSpider.class})
class VizierOfDefermentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that attacked this turn")
    void exilesCreatureThatAttackedThisTurn() {
        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true); // attacked this turn
        UUID attackerId = attacker.getId();

        castVizierToMayPrompt(player1, attackerId);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Pouncing Cheetah"));
    }

    @Test
    @DisplayName("Exiles a creature that blocked this turn — status survives into the postcombat main phase")
    void exilesCreatureThatBlockedThisTurn() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GiantSpider()); // blocker
        declareBlock();

        UUID blockerId = harness.getPermanentId(player2, "Giant Spider");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        castVizierToMayPrompt(player1, blockerId);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Exiled creature returns under its owner's control at the next end step")
    void exiledCreatureReturnsAtNextEndStep() {
        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true);
        UUID attackerId = attacker.getId();

        castVizierToMayPrompt(player1, attackerId);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Pouncing Cheetah");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Pouncing Cheetah"));
    }

    @Test
    @DisplayName("Can target a creature that did not attack or block, but does not exile it")
    void noncombatCreatureIsLegalTargetButIsNotExiled() {
        Permanent creature = addCreatureReady(player2, new PouncingCheetah());

        castVizierToMayPrompt(player1, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Pouncing Cheetah");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature on the battlefield")
    void decliningMayDoesNotExile() {
        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true);

        castVizierToMayPrompt(player1, attacker.getId());
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Pouncing Cheetah");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Pouncing Cheetah"));
    }

    @Test
    void canTargetVizierItselfWithoutExilingIt() {
        harness.castFromHand(player1, new VizierOfDeferment(), "{2}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Vizier of Deferment"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Vizier of Deferment");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void canExileAnAttackerAfterItLeavesCombat() {
        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true);
        attacker.setAttacking(false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        castVizierToMayPrompt(player1, attacker.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");
    }

    @Test
    void stolenCreatureReturnsToItsOwnerAsANewPermanent() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        attacker.setAttacking(true);
        gd.stolenCreatures.put(attacker.getId(), player2.getId());

        castVizierToMayPrompt(player1, attacker.getId());
        harness.handleMayAbilityChosen(player1, true);
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Pouncing Cheetah");
        Permanent returned = findPermanent(player2, "Pouncing Cheetah");
        assertThat(returned.getId()).isNotEqualTo(attacker.getId());
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void exileDuringEndStepWaitsForTheFollowingTurnsEndStep() {
        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.END_STEP);

        castVizierToMayPrompt(player1, attacker.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        harness.assertOnBattlefield(player2, "Pouncing Cheetah");
    }

    @Test
    @CardUsed({NinjaOfTheDeepHours.class})
    void creatureEnteringAttackingCannotBeExiledUnlessItActuallyAttackedOrBlocked() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        addCreatureReady(player2, new GiantSpider());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NinjaOfTheDeepHours()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        Permanent ninja = findPermanent(player1, "Ninja of the Deep Hours");

        castVizierToMayPrompt(player1, ninja.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ninja of the Deep Hours");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castVizierToMayPrompt(Player caster, UUID targetId) {
        harness.castFromHand(caster, new VizierOfDeferment(), "{2}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(caster, targetId);
        harness.passBothPriorities();
    }

    private void declareBlock() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
    }
}
