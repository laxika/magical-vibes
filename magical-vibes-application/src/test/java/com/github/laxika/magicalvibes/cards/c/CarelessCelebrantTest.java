package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarelessCelebrant.class, FlameJavelin.class, LlanowarElves.class})
class CarelessCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("When Careless Celebrant dies, it deals 2 damage to an opponent's creature")
    void deathTriggerDamagesOpponentsCreature() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        killCelebrantWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("When Careless Celebrant dies, its trigger cannot target your creature")
    void deathTriggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID ownCreatureId = harness.getPermanentId(player1, "Llanowar Elves");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Llanowar Elves");

        killCelebrantWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreatureId)
                .doesNotContain(ownCreatureId);
    }

    private void killCelebrantWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 5);

        UUID celebrantId = harness.getPermanentId(player1, "Careless Celebrant");
        harness.castInstant(player2, 0, celebrantId);
        harness.passBothPriorities();
    }
}
