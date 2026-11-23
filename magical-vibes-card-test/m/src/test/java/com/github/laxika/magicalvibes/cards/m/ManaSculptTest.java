package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSculpt.class, BurstLightning.class, FugitiveWizard.class, Ornithopter.class})
class ManaSculptTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a kicked spell and adds mana equal to what was paid at the next main phase")
    void countersKickedSpellAndAddsManaEqualToActualPaymentAtNextMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new FugitiveWizard());

        BurstLightning burstLightning = new BurstLightning();
        harness.setHand(player1, List.of(burstLightning));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burstLightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counters the spell but schedules no mana without a Wizard")
    void countersSpellButDoesNotScheduleManaWithoutAWizard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        BurstLightning burstLightning = new BurstLightning();
        harness.setHand(player1, List.of(burstLightning));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burstLightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Wizard does not enable the mana")
    void opponentsWizardDoesNotEnableManaReward() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new FugitiveWizard());
        BurstLightning spell = new BurstLightning();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        harness.assertLife(player2, 20);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).isEmpty();
    }

    @Test
    @DisplayName("The Wizard must still be controlled as Mana Sculpt resolves")
    void wizardMustStillBeControlledWhenManaSculptResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        BurstLightning spell = new BurstLightning();
        harness.setHand(player1, List.of(spell, new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, wizard.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        harness.assertLife(player2, 20);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).isEmpty();
    }

    @Test
    @DisplayName("The mana waits for its controller's next main phase after the Wizard dies")
    void rewardRemainsAfterWizardDiesAndWaitsForControllersMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        BurstLightning spell = new BurstLightning();
        harness.setHand(player1, List.of(spell, new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, wizard.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fugitive Wizard");

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Countering a spell cast for no mana still makes the delayed ability, which adds no mana")
    void spellCastForNoManaStillMakesTheDelayedAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new FugitiveWizard());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ornithopter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE)).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
