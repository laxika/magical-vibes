package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WarlordsFury;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdelizTheCinderWind.class, FugitiveWizard.class, GrizzlyBears.class, Shock.class, WarlordsFury.class})
class AdelizTheCinderWindTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts all Wizards you control +1/+1")
    void castingInstantBoostsWizards() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());

        // Resolve spell cast trigger
        harness.passBothPriorities();

        // Both Adeliz (Human Wizard) and Fugitive Wizard should get +1/+1
        assertThat(adeliz.getPowerModifier()).isEqualTo(1);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(1);
        assertThat(wizard.getPowerModifier()).isEqualTo(1);
        assertThat(wizard.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an instant does not boost non-Wizard creatures")
    void castingInstantDoesNotBoostNonWizards() {
        addCreatureReady(player1, new AdelizTheCinderWind());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Grizzly Bears (Bear) should not get the boost
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a creature does not trigger the boost")
    void castingCreatureDoesNotTrigger() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(0);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple instant casts stack the boost")
    void multipleInstantCastsStackBoost() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast first instant
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(1);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(1);

        // Resolve Shock
        harness.passBothPriorities();

        // Cast second instant
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(2);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's Wizards")
    void doesNotBoostOpponentWizards() {
        addCreatureReady(player1, new AdelizTheCinderWind());
        Permanent opponentWizard = addCreatureReady(player2, new FugitiveWizard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Opponent's Wizard should not get the boost
        assertThat(opponentWizard.getPowerModifier()).isEqualTo(0);
        assertThat(opponentWizard.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a sorcery boosts Wizards before the spell resolves")
    void castingSorceryBoostsWizards() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new WarlordsFury()));

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(1);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Adeliz")
    void opponentInstantDoesNotTrigger() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isZero();
        assertThat(adeliz.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(adeliz.getPowerModifier()).isEqualTo(1);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(adeliz.getPowerModifier()).isZero();
        assertThat(adeliz.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only Wizards present when the trigger resolves receive its boost")
    void boostUsesWizardsPresentAtResolution() {
        addCreatureReady(player1, new AdelizTheCinderWind());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());

        Permanent beforeResolution = addCreatureReady(player1, new FugitiveWizard());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new FugitiveWizard());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The trigger still boosts other Wizards if Adeliz dies in response")
    void triggerResolvesAfterAdelizDies() {
        Permanent adeliz = addCreatureReady(player1, new AdelizTheCinderWind());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, adeliz.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Adeliz, the Cinder Wind");

        harness.passBothPriorities();

        assertThat(wizard.getPowerModifier()).isEqualTo(1);
        assertThat(wizard.getToughnessModifier()).isEqualTo(1);
    }
}
