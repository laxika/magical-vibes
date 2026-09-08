package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TundraKavu.class, Forest.class})
class TundraKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability offers only Plains or Island")
    void resolvingOffersOnlyPlainsOrIsland() {
        Permanent forest = addKavuAndForest();

        activateAbility(forest);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("PLAINS", "ISLAND");
    }

    @Test
    @DisplayName("Choosing Plains replaces the target land's type")
    void choosingPlainsReplacesLandType() {
        Permanent forest = addKavuAndForest();

        activateAbility(forest);
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Choosing Island replaces the target land's type until end of turn")
    void choosingIslandReplacesLandTypeUntilEndOfTurn() {
        Permanent forest = addKavuAndForest();

        activateAbility(forest);
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    private Permanent addKavuAndForest() {
        addCreatureReady(player1, new TundraKavu());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        return forest;
    }

    private void activateAbility(Permanent forest) {
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
    }
}
