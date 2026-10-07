package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranPowerSuit.class, GrizzlyBears.class, LeoninScimitar.class, Pacifism.class,
        Shock.class, IcyManipulator.class})
class ThranPowerSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each attached Aura and Equipment")
    void equippedCreatureScalesWithAttachments() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ward {2} is granted to the equipped creature")
    void wardProtectsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip {2} attaches Thran Power Suit to a creature you control")
    void equipAttachesToCreature() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void countsOpponentsAuraAndStopsCountingRemovedAttachments() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherSuit = harness.addToBattlefieldAndReturn(player2, new ThranPowerSuit());
        otherSuit.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void wardCanBePaidToLetSpellResolve() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardPaymentCanBeDeclinedDespiteAvailableMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardDoesNotTriggerForControllersOwnSpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoSuitsGrantSeparateWardPaymentsAndEachCountsBothEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void movingEquipmentRemovesBoostAndWardFromPreviousCreature() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardBelongsToCreatureEvenWhenOpponentControlsEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void suitItselfDoesNotHaveWard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        suit.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, suit.getId());
        harness.passBothPriorities();

        assertThat(suit.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
