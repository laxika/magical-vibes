package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FesteringMummyTest extends BaseCardTest {

    /**
     * Sets up combat where Festering Mummy (player1, 1/1) attacks and is blocked by a 3/3 creature
     * (player2). Festering Mummy dies from combat damage.
     */
    private void setupCombatWhereMummyDies() {
        Permanent mummyPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Festering Mummy"))
                .findFirst().orElseThrow();
        mummyPerm.setSummoningSick(false);
        mummyPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When Festering Mummy dies, controller is prompted to choose a target creature (CR 603.3d)")
    void deathTriggerPromptsTargetChoice() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereMummyDies();

        harness.passBothPriorities(); // Combat damage — Mummy dies, target selection prompt

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Festering Mummy"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may puts a -1/-1 counter on the target 2/2, reducing it to 1/1")
    void deathTriggerPutsCounterOnTarget() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("A -1/-1 counter kills a 1/1 target creature")
    void deathTriggerKillsOneOneCreature() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, elvesId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Declining the may leaves the target creature untouched")
    void decliningMayDoesNotPutCounter() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, false); // Decline may

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Festering Mummy"));

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Triggered ability fizzles when the target creature leaves before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities(); // Resolve — target gone, fizzles

        assertThat(gd.stack).isEmpty();
    }
}
