package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FirefrightMage;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyrohemia.class, FirefrightMage.class, KavuPredator.class})
class PyrohemiaTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addToBattlefield(player1, new FirefrightMage());
        harness.addToBattlefield(player2, new FirefrightMage());
        harness.addToBattlefield(player2, new KavuPredator());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Firefright Mage");
        harness.assertNotOnBattlefield(player2, "Firefright Mage");
        harness.assertOnBattlefield(player2, "Kavu Predator");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can activate without creatures and still damages both players")
    void activatedAbilityWorksWithoutCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Pyrohemia");
    }

    @Test
    @DisplayName("Can activate repeatedly and accumulated damage kills creatures")
    void repeatedActivationsAccumulateDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addToBattlefield(player1, new KavuPredator());
        harness.addToBattlefield(player2, new KavuPredator());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kavu Predator");
        harness.assertOnBattlefield(player2, "Kavu Predator");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kavu Predator");
        harness.assertNotOnBattlefield(player2, "Kavu Predator");
        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Kavu Predator");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Pyrohemia");
    }

    @Test
    @DisplayName("Does not trigger when the last creature dies after the end step begins")
    void doesNotTriggerWhenLastCreatureDiesDuringEndStep() {
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addToBattlefield(player2, new FirefrightMage());

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Firefright Mage");
        harness.assertOnBattlefield(player1, "Pyrohemia");
        harness.assertNotInGraveyard(player1, "Pyrohemia");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself at end step when no creatures are on the battlefield")
    void sacrificesAtEndStepWhenNoCreatures() {
        harness.addToBattlefield(player1, new Pyrohemia());

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pyrohemia");
        harness.assertInGraveyard(player1, "Pyrohemia");
    }

    @Test
    @DisplayName("Does not sacrifice itself while a creature is on the battlefield")
    void doesNotSacrificeWhenCreaturePresent() {
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addToBattlefield(player2, new KavuPredator());

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Pyrohemia"));
        harness.assertOnBattlefield(player1, "Pyrohemia");
    }

    @Test
    @DisplayName("Sacrifices itself at an opponent's end step when no creatures are on the battlefield")
    void sacrificesAtOpponentsEndStepWhenNoCreatures() {
        harness.addToBattlefield(player1, new Pyrohemia());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pyrohemia");
        harness.assertInGraveyard(player1, "Pyrohemia");
    }

    @Test
    @DisplayName("Does not sacrifice itself if a creature appears before the trigger resolves")
    void doesNotSacrificeIfCreatureAppearsBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new Pyrohemia());

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new FirefrightMage());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pyrohemia");
        harness.assertNotInGraveyard(player1, "Pyrohemia");
    }
}
