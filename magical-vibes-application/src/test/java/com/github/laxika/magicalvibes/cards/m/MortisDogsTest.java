package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.c.CagedSun;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MortisDogs.class, PhyrexianHulk.class, CagedSun.class})
class MortisDogsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent dogs = addCreatureReady(player1, new MortisDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(dogs.getPowerModifier()).isEqualTo(2);
        assertThat(dogs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("When Mortis Dogs dies, controller is prompted to choose a target player")
    void deathTriggerPromptsForTargetPlayer() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player2, 20);

        setupCombatWhereMortisDogsDies();
        resolveCombat();

        // Mortis Dogs should be in graveyard
        harness.assertInGraveyard(player1, "Mortis Dogs");

        // Player1 should be prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger causes target player to lose life equal to base power (2) when not boosted")
    void deathTriggerLosesLifeEqualToBasePower() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereMortisDogsDies();
        resolveCombat();

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the death trigger
        harness.passBothPriorities();

        // Opponent loses 2 life (base power): 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Death trigger uses boosted power (4) when Mortis Dogs has +2/+0 modifier")
    void deathTriggerUsesBoostedPower() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Simulate the attack boost (+2/+0) that would have been applied by the ON_ATTACK trigger
        Permanent dogsPerm = findPermanent(player1, "Mortis Dogs");
        dogsPerm.setPowerModifier(2);

        setupCombatWhereMortisDogsDies();
        resolveCombat();

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the death trigger
        harness.passBothPriorities();

        // Death trigger uses boosted power (2 base + 2 modifier = 4): 20 - 4 = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Death trigger can target the controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereMortisDogsDies();
        resolveCombat();

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());

        // Resolve the death trigger
        harness.passBothPriorities();

        // Controller loses 2 life: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Opponent unaffected
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Death trigger includes continuous power bonuses at death")
    void deathTriggerIncludesContinuousPowerBonus() {
        harness.addToBattlefield(player1, new MortisDogs());
        Permanent sun = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        sun.setChosenColor(CardColor.BLACK);
        Permanent dogs = findPermanent(player1, "Mortis Dogs");
        assertThat(gqs.getEffectivePower(gd, dogs)).isEqualTo(3);
        harness.setLife(player2, 20);

        addCreatureReady(player2, new PhyrexianHulk());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        harness.assertInGraveyard(player1, "Mortis Dogs");
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Resolved attack boost is included in the death trigger after lethal combat")
    void attackBoostIsIncludedWhenDogsDiesInCombat() {
        Permanent dogs = addCreatureReady(player1, new MortisDogs());
        addCreatureReady(player2, new PhyrexianHulk());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, dogs)).isEqualTo(4);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertInGraveyard(player1, "Mortis Dogs");
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Attack boost expires at end of turn")
    void attackBoostExpiresAtEndOfTurn() {
        Permanent dogs = addCreatureReady(player1, new MortisDogs());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, dogs)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dogs)).isEqualTo(2);
    }

    /**
     * Sets up combat where Mortis Dogs attacks and is blocked by Phyrexian Hulk.
     * Mortis Dogs will die from combat damage (does NOT get the attack boost since we skip declare attackers).
     */
    private void setupCombatWhereMortisDogsDies() {
        Permanent dogsPerm = findPermanent(player1, "Mortis Dogs");
        dogsPerm.setSummoningSick(false);
        dogsPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new PhyrexianHulk());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }
}
