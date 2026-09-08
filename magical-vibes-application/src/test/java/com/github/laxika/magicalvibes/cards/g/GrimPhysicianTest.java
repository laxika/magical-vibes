package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimPhysician.class, GrizzlyBears.class})
class GrimPhysicianTest extends BaseCardTest {

    @Test
    @DisplayName("When Grim Physician dies, it targets an opponent's creature")
    void deathTriggerTargetsOpponentsCreature() {
        harness.addToBattlefield(player1, new GrimPhysician());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        setupCombatWherePhysicianDies();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst()
                .orElseThrow();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("When Grim Physician dies, its trigger cannot target a creature you control")
    void deathTriggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrimPhysician());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        setupCombatWherePhysicianDies();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(ownCreatureId);
    }

    private void setupCombatWherePhysicianDies() {
        Permanent physician = findPermanent(player1, "Grim Physician");
        physician.setSummoningSick(false);
        physician.setAttacking(true);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(3);
        blockerCard.setToughness(3);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
