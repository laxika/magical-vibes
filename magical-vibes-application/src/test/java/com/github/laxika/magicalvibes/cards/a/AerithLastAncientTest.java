package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerithLastAncient.class, GrizzlyBears.class, Shock.class})
class AerithLastAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the targeted creature to hand after gaining life")
    void returnsTargetToHandBelowSevenLife() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 6);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns the targeted creature to the battlefield after gaining 7 life")
    void returnsTargetToBattlefieldAtSevenLife() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when no life was gained")
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not target noncreature cards")
    void doesNotTargetNoncreatureCards() {
        harness.addToBattlefield(player1, new AerithLastAncient());
        harness.setGraveyard(player1, List.of(new Shock()));
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    private void chooseTarget(GrizzlyBears target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
