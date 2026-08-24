package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiremawKavu.class, ColossalDreadmaw.class})
class FiremawKavuTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to its target and leaves-the-battlefield deals 4 to a new target")
    void entersAndLeavesWithIndependentDamageTargets() {
        Permanent enterTarget = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent leaveTarget = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent firemaw = castFiremaw(enterTarget.getId());

        assertThat(enterTarget.getMarkedDamage()).isEqualTo(2);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firemaw));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, leaveTarget.getId());
        harness.passBothPriorities();

        assertThat(leaveTarget.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining echo sacrifices Firemaw Kavu")
    void decliningEchoSacrificesFiremawKavu() {
        Permanent target = addCreatureReady(player2, new ColossalDreadmaw());
        castFiremaw(target.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Firemaw Kavu");
        harness.assertInGraveyard(player1, "Firemaw Kavu");
    }

    private Permanent castFiremaw(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FiremawKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Firemaw Kavu");
    }
}
