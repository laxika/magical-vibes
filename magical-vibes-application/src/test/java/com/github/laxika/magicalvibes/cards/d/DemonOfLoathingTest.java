package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonOfLoathing.class, GrizzlyBears.class})
class DemonOfLoathingTest extends BaseCardTest {

    @Test
    void damagedPlayerChoosesACreatureToSacrifice() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondEnemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(enemyCreature.getId(), secondEnemyCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, enemyCreature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void blockedDemonDoesNotTrigger() {
        DemonOfLoathing demonCard = new DemonOfLoathing();
        demonCard.setPower(2);
        Permanent demon = addCreatureReady(player1, demonCard);
        demon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noTriggerWhenDamagedPlayerControlsNoCreatures() {
        Permanent demon = addCreatureReady(player1, new DemonOfLoathing());
        demon.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
