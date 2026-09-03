package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pestilence.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class})
class PestilenceTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pestilence());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pestilence");
        harness.assertNotOnBattlefield(player2, "Merfolk of the Pearl Trident");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can be activated more than once without tapping")
    void canBeActivatedMoreThanOnceWithoutTapping() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pestilence());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pestilence");
        harness.assertNotOnBattlefield(player2, "Merfolk of the Pearl Trident");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sacrifices itself at end step when no creatures are on the battlefield")
    void sacrificesAtEndStepWhenNoCreatures() {
        harness.addToBattlefield(player1, new Pestilence());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pestilence");
        harness.assertInGraveyard(player1, "Pestilence");
    }

    @Test
    @DisplayName("Does not sacrifice itself while a creature is on the battlefield")
    void doesNotSacrificeWhenCreaturePresent() {
        harness.addToBattlefield(player1, new Pestilence());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        // The intervening-if fails (a creature is present), so no sacrifice trigger is put on the
        // stack and Pestilence survives past the end step.
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Pestilence"));
        harness.assertOnBattlefield(player1, "Pestilence");
    }

    @Test
    @DisplayName("Triggers during an opponent's end step when no creatures are on the battlefield")
    void sacrificesAtOpponentEndStepWhenNoCreatures() {
        harness.addToBattlefield(player1, new Pestilence());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pestilence");
        harness.assertInGraveyard(player1, "Pestilence");
    }

    @Test
    @DisplayName("Does not sacrifice if a creature enters before its trigger resolves")
    void doesNotSacrificeIfCreatureEntersBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new Pestilence());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pestilence");
        harness.assertNotInGraveyard(player1, "Pestilence");
    }
}
