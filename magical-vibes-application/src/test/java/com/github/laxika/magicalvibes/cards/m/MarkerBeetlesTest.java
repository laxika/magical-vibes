package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarkerBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("When Marker Beetles dies, target creature gets +1/+1 until end of turn")
    void deathTriggerBoostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new MarkerBeetles());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Permanent beetles = findPermanent(player1, "Marker Beetles");
        beetles.setSummoningSick(false);
        beetles.setAttacking(true);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(3);
        blockerCard.setToughness(3);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = permanentById(player2.getId(), targetId);
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("{2}, Sacrifice Marker Beetles: Draw a card")
    void sacrificeAbilityDrawsACard() {
        harness.addToBattlefield(player1, new MarkerBeetles());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marker Beetles");
        harness.assertInGraveyard(player1, "Marker Beetles");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent permanentById(UUID ownerId, UUID id) {
        GameData gd = harness.getGameData();
        return gd.playerBattlefields.get(ownerId).stream()
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
