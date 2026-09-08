package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FireUrchinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant gives Fire Urchin +1/+0 until end of turn")
    void instantBoostsFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery gives Fire Urchin +1/+0 until end of turn")
    void sorceryBoostsFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature does not boost Fire Urchin")
    void creatureDoesNotBoostFireUrchin() {
        Permanent fireUrchin = harness.addToBattlefieldAndReturn(player1, new FireUrchin());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fireUrchin)).isEqualTo(1);
    }
}
