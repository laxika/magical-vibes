package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MudbuttonTorchrunner.class, WoodlandChangeling.class, GarrukWildspeaker.class})
class MudbuttonTorchrunnerTest extends BaseCardTest {

    /**
     * Sets up combat where Mudbutton Torchrunner (player1, 1/1) attacks and is blocked by
     * a 2/2 Woodland Changeling (player2), so the Torchrunner dies from combat damage.
     */
    private void setupCombatWhereTorchrunnerDies() {
        Permanent torchrunner = findPermanent(player1, "Mudbutton Torchrunner");
        torchrunner.setSummoningSick(false);
        torchrunner.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Death trigger deals 3 damage to chosen creature and destroys it if lethal")
    void deathTriggerDeals3DamageAndKillsCreature() {
        harness.addToBattlefield(player1, new MudbuttonTorchrunner());
        harness.addToBattlefield(player2, new WoodlandChangeling());

        UUID bearsId = harness.getPermanentId(player2, "Woodland Changeling");

        setupCombatWhereTorchrunnerDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Mudbutton Torchrunner");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Death trigger deals 3 damage to chosen player")
    void deathTriggerDeals3DamageToPlayer() {
        harness.addToBattlefield(player1, new MudbuttonTorchrunner());
        harness.setLife(player2, 20);

        setupCombatWhereTorchrunnerDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
    @Test
    @DisplayName("Death trigger can target its own controller")
    void deathTriggerDealsDamageToController() {
        harness.addToBattlefield(player1, new MudbuttonTorchrunner());
        harness.setLife(player1, 20);

        setupCombatWhereTorchrunnerDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger deals damage to a planeswalker")
    void deathTriggerKillsPlaneswalkerWithThreeLoyalty() {
        harness.addToBattlefield(player1, new MudbuttonTorchrunner());
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 3);

        setupCombatWhereTorchrunnerDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, garruk.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
        harness.assertLife(player2, 20);
    }
}
