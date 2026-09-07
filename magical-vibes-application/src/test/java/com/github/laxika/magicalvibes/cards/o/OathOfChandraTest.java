package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfChandra.class, GideonOfTheTrials.class, GrizzlyBears.class})
class OathOfChandraTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, deals 3 damage to target creature an opponent controls")
    void entersAndDealsDamageToOpponentCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOathOfChandra();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability only offers creatures an opponent controls")
    void etbOnlyTargetsOpponentCreatures() {
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOathOfChandra();

        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("At each end step, deals 2 damage to each opponent after a planeswalker enters under its controller's control")
    void dealsDamageAtEndStepAfterPlaneswalkerEnters() {
        harness.addToBattlefield(player1, new OathOfChandra());
        castGideonOfTheTrials(player1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger at end step without a planeswalker entering under its controller's control")
    void doesNotTriggerWithoutControlledPlaneswalkerEntry() {
        harness.addToBattlefield(player1, new OathOfChandra());
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castOathOfChandra() {
        harness.setHand(player1, List.of(new OathOfChandra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
    }

    private void castGideonOfTheTrials(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new GideonOfTheTrials()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castPlaneswalker(player, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
