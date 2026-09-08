package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GurmagRakshasa.class, GrizzlyBears.class, HillGiant.class})
class GurmagRakshasaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -2/-2 and your creature +2/+2")
    void etbAppliesBothModifiers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0, List.of(opposingCreatureId, ownCreatureId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCreature = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modifiers wear off at end of turn")
    void modifiersWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0, List.of(opposingCreatureId, ownCreatureId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Each target must have the required controller")
    void enforcesTargetControllers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownCreatureId, opposingCreatureId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
