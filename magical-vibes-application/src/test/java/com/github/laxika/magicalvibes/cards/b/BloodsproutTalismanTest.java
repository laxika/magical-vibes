package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodsproutTalisman.class, GrizzlyBears.class, Forest.class})
class BloodsproutTalismanTest extends BaseCardTest {

    @Test
    void entersTapped() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());

        assertThat(talisman.isTapped()).isTrue();
    }

    @Test
    void choosesNonlandCardAndMakesItCostOneLessPerpetually() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        GrizzlyBears chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Forest(), chosenCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PerpetualCastCostHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualCastCostHandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
