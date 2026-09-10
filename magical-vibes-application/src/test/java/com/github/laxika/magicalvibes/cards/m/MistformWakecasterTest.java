package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistformWakecaster.class, GrizzlyBears.class})
class MistformWakecasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability changes Mistform Wakecaster to the chosen type")
    void changesSelfToChosenType() {
        Permanent wakecaster = addWakecaster();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The second ability changes each creature you control and not an opponent's creature")
    void changesControlledCreaturesOnly() {
        Permanent wakecaster = addWakecaster();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.WALL.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.WALL);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).containsExactly(CardSubtype.WALL);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingCreature)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The creature type change wears off at end of turn")
    void changesWearOffAtEndOfTurn() {
        Permanent wakecaster = addWakecaster();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.ILLUSION);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).containsExactly(CardSubtype.BEAR);
    }

    private Permanent addWakecaster() {
        Permanent wakecaster = harness.addToBattlefieldAndReturn(player1, new MistformWakecaster());
        wakecaster.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        return wakecaster;
    }
}
