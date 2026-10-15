package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Paralyze.class, GrizzlyBears.class, SolRing.class, Disenchant.class})
class ParalyzeTest extends BaseCardTest {

    // ===== ETB tap =====

    @Test
    @DisplayName("Resolving Paralyze taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Paralyze()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paralyze")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Paralyze cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new Paralyze()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The ETB tap uses the creature currently enchanted when it resolves")
    void etbTapsCurrentEnchantedCreature() {
        Permanent originalTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent currentEnchantedCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Paralyze()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, originalTarget.getId());
        harness.passBothPriorities(); // resolve the Aura spell

        Permanent aura = findPermanent(player1, "Paralyze");
        aura.setAttachedTo(currentEnchantedCreature.getId());

        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(originalTarget.isTapped()).isFalse();
        assertThat(currentEnchantedCreature.isTapped()).isTrue();
    }

    // ===== Doesn't untap =====

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        attachParalyze(creature);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Upkeep: may pay {4} to untap =====

    @Test
    @DisplayName("Enchanted creature's controller pays {4} at upkeep to untap it")
    void controllerPaysToUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.addMana(player2, ManaColor.BLACK, 4); // mana available at payment time
        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the {4} payment leaves the creature tapped")
    void decliningLeavesTapped() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.addMana(player2, ManaColor.BLACK, 4); // could pay, but declines
        harness.handleMayAbilityChosen(player2, false);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting without enough mana leaves the creature tapped")
    void cannotPayLeavesTapped() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        // No mana for player2.
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep untap ability does not fire during the Aura controller's upkeep")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        harness.addMana(player1, ManaColor.BLACK, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // No may-pay prompt for player1; creature stays tapped.
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Helpers =====

    @Test
    @DisplayName("Destroying the Aura in response does not prevent the upkeep payment from untapping its former creature")
    void upkeepPaymentStillUntapsAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);
        Permanent aura = findPermanent(player1, "Paralyze");

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Paralyze");
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The Aura controller's mana cannot pay the enchanted creature controller's upkeep cost")
    void auraControllerManaCannotPayForOpponent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing Paralyze before untap lets the creature untap normally without a payment")
    void removingAuraRestoresNormalUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);
        Permanent aura = findPermanent(player1, "Paralyze");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Paralyze");

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private void attachParalyze(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Paralyze());
        aura.setAttachedTo(creature.getId());
    }
}
