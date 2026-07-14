package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class DelusionsOfMediocrityTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 10 life")
    void entryGainsTenLife() {
        harness.setHand(player1, List.of(new DelusionsOfMediocrity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves

        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("Leaving the battlefield loses 10 life")
    void leavingLosesTenLife() {
        harness.addToBattlefield(player1, new DelusionsOfMediocrity());
        harness.setLife(player1, 30);

        Permanent delusions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof DelusionsOfMediocrity)
                .findFirst().orElseThrow();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, delusions);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger resolves

        harness.assertLife(player1, 20);
    }
}
