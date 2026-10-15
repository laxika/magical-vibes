package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitanHunter.class, GrizzlyBears.class})
class TitanHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each end-step player when no creature died")
    void damagesEndStepPlayerWhenNoCreatureDied() {
        harness.addToBattlefield(player1, new TitanHunter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger when a creature died this turn")
    void doesNotTriggerAfterCreatureDeath() {
        harness.addToBattlefield(player1, new TitanHunter());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a creature gains 4 life")
    void sacrificeCreatureGainsFourLife() {
        harness.addToBattlefield(player1, new TitanHunter());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Titan Hunter");
    }

    @Test
    @DisplayName("Deals damage during its controller's end step too")
    void damagesControllerDuringTheirEndStep() {
        harness.addToBattlefield(player1, new TitanHunter());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature sacrificed in response prevents end-step damage")
    void sacrificeInResponsePreventsDamage() {
        var hunter = harness.addToBattlefieldAndReturn(player1, new TitanHunter());
        harness.addToBattlefield(player1, new TitanHunter());

        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, hunter.getId());

        harness.assertInGraveyard(player1, "Titan Hunter");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Titan Hunter may sacrifice itself and its ability still gains life")
    void canSacrificeItselfAndSuppressRemainingHuntersTrigger() {
        var hunter = harness.addToBattlefieldAndReturn(player1, new TitanHunter());
        harness.addToBattlefield(player1, new TitanHunter());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, hunter.getId());
        harness.assertInGraveyard(player1, "Titan Hunter");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 24);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
