package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainAmericaFirstAvenger.class, GrizzlyBears.class, StriderHarness.class})
class CaptainAmericaFirstAvengerTest extends BaseCardTest {

    @Test
    void throwUsesAttachedEquipmentManaValueAndAllowsOpponentControlledEquipment() {
        Permanent captain = addReadyCaptain();
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new StriderHarness());
        equipment.setAttachedTo(captain.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void catchAttachesTargetEquipmentAtBeginningOfCombat() {
        Permanent captain = addReadyCaptain();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new StriderHarness());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(captain.getId());
    }

    @Test
    void throwCannotBePaidWithoutAnEquipmentAttachedToCaptain() {
        addReadyCaptain();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Equipment");
    }

    private Permanent addReadyCaptain() {
        return addCreatureReady(player1, new CaptainAmericaFirstAvenger());
    }
}
