package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoldenDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, all creatures get -2/-2 until end of turn")
    void weakensAllCreaturesWithoutBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndResolve();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With the city's blessing, only opponents' creatures get -2/-2")
    void weakensOnlyOpposingCreaturesWithBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GoldenDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
