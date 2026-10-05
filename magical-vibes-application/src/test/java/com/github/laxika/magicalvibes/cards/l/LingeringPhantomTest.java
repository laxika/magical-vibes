package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.MemorialToFolly;
import com.github.laxika.magicalvibes.cards.p.PutridImp;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LingeringPhantom.class, AdelizTheCinderWind.class, GrizzlyBears.class, Spellbook.class,
        MemorialToFolly.class, PutridImp.class, TheFlameOfKeld.class})
class LingeringPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact triggers may-pay prompt when Lingering Phantom is in graveyard")
    void artifactCastTriggersMayPayPrompt() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        // Stack: [Spellbook (bottom), MayPayMana trigger (top)]
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may-pay and paying {B} returns Lingering Phantom from graveyard to hand")
    void acceptAndPayReturnsToHand() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lingering Phantom");
        harness.assertNotInGraveyard(player1, "Lingering Phantom");
        // Black mana spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining may-pay keeps Lingering Phantom in graveyard")
    void declineKeepsInGraveyard() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Lingering Phantom");
        harness.assertInGraveyard(player1, "Lingering Phantom");
    }

    @Test
    @DisplayName("Casting a legendary creature triggers the graveyard ability")
    void legendaryCastTriggers() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lingering Phantom");
        harness.assertNotInGraveyard(player1, "Lingering Phantom");
    }

    @Test
    @DisplayName("Casting a non-historic spell does not trigger the graveyard ability")
    void nonHistoricDoesNotTrigger() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Lingering Phantom");
    }

    @Test
    @DisplayName("Opponent casting a historic spell does not trigger controller's graveyard phantom")
    void opponentHistoricDoesNotTrigger() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        // Only the artifact spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Lingering Phantom");
    }

    @Test
    @DisplayName("Does not trigger when Lingering Phantom is on the battlefield")
    void doesNotTriggerFromBattlefield() {
        harness.addToBattlefield(player1, new LingeringPhantom());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Only the artifact spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Accepting may-pay without enough mana treats as decline")
    void acceptWithoutManaIsDecline() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));
        // No black mana added

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept without mana

        // Phantom should remain in graveyard
        harness.assertInGraveyard(player1, "Lingering Phantom");
    }

    @Test
    @DisplayName("Casting a Saga returns the phantom before the Saga resolves")
    void sagaCastReturnsBeforeSagaResolves() {
        harness.setGraveyard(player1, List.of(new LingeringPhantom()));
        harness.setHand(player1, List.of(new TheFlameOfKeld()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lingering Phantom");
        harness.assertNotInGraveyard(player1, "Lingering Phantom");
        harness.assertNotOnBattlefield(player1, "The Flame of Keld");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each graveyard phantom has a separate optional payment")
    void multiplePhantomsReturnOnlyThePaidForCopy() {
        LingeringPhantom first = new LingeringPhantom();
        LingeringPhantom second = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c instanceof LingeringPhantom).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c instanceof LingeringPhantom).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c instanceof LingeringPhantom).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c instanceof LingeringPhantom).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("An old trigger cannot return a phantom that left and reentered the graveyard")
    void oldTriggerDoesNotReturnReenteredPhantom() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.addToBattlefield(player1, new MemorialToFolly());
        harness.addToBattlefield(player1, new PutridImp());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertInHand(player1, "Lingering Phantom");

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Lingering Phantom");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Lingering Phantom");
        harness.assertNotInHand(player1, "Lingering Phantom");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
