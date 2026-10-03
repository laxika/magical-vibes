package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZephyrGull;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonOfLoathing.class, ZephyrGull.class, Forest.class})
class DemonOfLoathingTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player chooses a creature they control to sacrifice")
    void damagedPlayerChoosesCreature() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new ZephyrGull());
        Permanent enemyCreature = addCreatureReady(player2, new ZephyrGull());
        Permanent otherEnemyCreature = addCreatureReady(player2, new ZephyrGull());
        harness.addToBattlefield(player2, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(enemyCreature.getId(), otherEnemyCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, enemyCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(otherEnemyCreature)
                .doesNotContain(enemyCreature);
        harness.assertInGraveyard(player2, "Zephyr Gull");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("No sacrifice trigger occurs when the Demon deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZephyrGull());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addToBattlefield(player2, new Forest());

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void resolvesWithoutSacrificeWhenDamagedPlayerControlsNoCreatures() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void trampleDamageTriggersSacrificeOfSurvivingCreature() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZephyrGull());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent survivor = addCreatureReady(player2, new ZephyrGull());

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 6));
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker, survivor);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(blocker.getCard(), survivor.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggerStillResolvesAfterDemonLeavesBattlefield() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent victim = addCreatureReady(player2, new ZephyrGull());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(demon);
        gd.playerGraveyards.get(player1.getId()).add(demon.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        harness.assertInGraveyard(player2, "Zephyr Gull");
    }

    @Test
    void creatureEnteringAfterDamageCanBeSacrificed() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        Permanent victim = addCreatureReady(player2, new ZephyrGull());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        harness.assertInGraveyard(player2, "Zephyr Gull");
    }
}
