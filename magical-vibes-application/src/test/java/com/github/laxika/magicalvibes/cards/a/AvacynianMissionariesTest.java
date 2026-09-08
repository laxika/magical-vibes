package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GhostfireBlade;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvacynianMissionariesTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at its controller's end step when equipped")
    void transformsWhenEquipped() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);

        resolveEndStepTransform();

        assertThat(missionaries.isTransformed()).isTrue();
        assertThat(missionaries.getCard().getName()).isEqualTo("Lunarch Inquisitors");
    }

    @Test
    @DisplayName("Does not transform at its controller's end step when not equipped")
    void doesNotTransformWhenNotEquipped() {
        Permanent missionaries = addMissionaries();

        resolveEndStepTransform();

        assertThat(missionaries.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May exile another creature until Lunarch Inquisitors leaves")
    void mayExileAnotherCreatureUntilItLeaves() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());

        resolveEndStepTransform();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertNotOnBattlefield(player2, "Goblin Piker");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, missionaries.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Piker");
    }

    private Permanent addMissionaries() {
        Permanent missionaries = harness.addToBattlefieldAndReturn(player1, new AvacynianMissionaries());
        missionaries.setSummoningSick(false);
        return missionaries;
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GhostfireBlade());
        equipment.setAttachedTo(creature.getId());
    }

    private void resolveEndStepTransform() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
