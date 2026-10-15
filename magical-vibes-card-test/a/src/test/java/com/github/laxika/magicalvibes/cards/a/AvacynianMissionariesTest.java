package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.s.SeagrafSkaab;
import com.github.laxika.magicalvibes.cards.t.TrueFaithCenser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvacynianMissionaries.class, TrueFaithCenser.class, SeagrafSkaab.class, JustTheWind.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeagrafSkaab());

        resolveEndStepTransform();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Seagraf Skaab");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, missionaries.getId());

        harness.assertOnBattlefield(player2, "Seagraf Skaab");
    }

    @Test
    @DisplayName("Transforming with no other creature does not offer an exile choice")
    void noExileChoiceWithoutLegalTarget() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);

        resolveEndStepTransform();

        assertThat(missionaries.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not transform during an opponent's end step")
    void doesNotTransformDuringOpponentsEndStep() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(missionaries.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not transform if equipment is unattached before the trigger resolves")
    void rechecksEquippedOnResolution() {
        Permanent missionaries = addMissionaries();
        Permanent equipment = attachEquipment(missionaries);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(missionaries.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses a target before deciding at resolution whether to exile it")
    void mayDeclineExileOnResolution() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeagrafSkaab());

        resolveEndStepTransform();
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Seagraf Skaab");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(missionaries.isTransformed()).isTrue();
        harness.assertOnBattlefield(player2, "Seagraf Skaab");
    }

    @Test
    @DisplayName("The transform trigger cannot target Inquisitors itself or an Equipment")
    void cannotTargetSelfOrNoncreature() {
        Permanent missionaries = addMissionaries();
        Permanent equipment = attachEquipment(missionaries);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeagrafSkaab());

        resolveEndStepTransform();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, missionaries.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Lunarch Inquisitors");
        harness.assertOnBattlefield(player1, "True-Faith Censer");
        harness.assertNotOnBattlefield(player2, "Seagraf Skaab");
    }

    @Test
    @DisplayName("Can exile another creature controlled by its controller")
    void mayExileOwnCreature() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeagrafSkaab());

        resolveEndStepTransform();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Seagraf Skaab");
        harness.assertOnBattlefield(player1, "Lunarch Inquisitors");
    }

    @Test
    @DisplayName("Does not exile the target if Inquisitors leaves before its ability resolves")
    void sourceLeavingInResponsePreventsExile() {
        Permanent missionaries = addMissionaries();
        attachEquipment(missionaries);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeagrafSkaab());

        resolveEndStepTransform();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, missionaries.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInHand(player1, "Avacynian Missionaries");
        harness.assertOnBattlefield(player2, "Seagraf Skaab");
    }

    private Permanent addMissionaries() {
        return harness.addToBattlefieldAndReturn(player1, new AvacynianMissionaries());
    }

    private Permanent attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrueFaithCenser());
        equipment.setAttachedTo(creature.getId());
        return equipment;
    }

    private void resolveEndStepTransform() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
