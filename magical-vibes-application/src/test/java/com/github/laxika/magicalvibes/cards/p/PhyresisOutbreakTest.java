package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyresisOutbreak.class, HillGiant.class})
class PhyresisOutbreakTest extends BaseCardTest {

    @Test
    @DisplayName("Gives opponents poison, then shrinks their creatures by their poison total")
    void poisonsOpponentsAndShrinksTheirCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new PhyresisOutbreak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getToughnessModifier()).isZero();
    }
}
