package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChaosChanneler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrimWanderer;
import com.github.laxika.magicalvibes.cards.w.WizardMentor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobeOfTheArchmagi.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class,
        WizardMentor.class, GrimWanderer.class, ChaosChanneler.class})
class RobeOfTheArchmagiTest extends BaseCardTest {

    @Test
    @DisplayName("The restricted equip ability attaches Robe of the Archmagi to a Wizard")
    void restrictedEquipAttachesToWizard() {
        Permanent robe = addRobeReady();
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe), 1,
                null, wizard.getId());
        harness.passBothPriorities();

        assertThat(robe.getAttachedTo()).isEqualTo(wizard.getId());
    }

    @Test
    @DisplayName("The restricted equip ability cannot target a nonmatching creature")
    void restrictedEquipRejectsNonmatchingCreature() {
        Permanent robe = addRobeReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe), 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Shaman, Warlock, or Wizard");
    }

    @Test
    @DisplayName("Equipped creature's combat damage draws that many cards")
    void combatDamageDrawsThatManyCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent creature = addCreatureReady(player1, new WizardMentor());
        Permanent robe = addRobeReady();
        robe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The full-price equip ability can attach to a creature of any subtype")
    void normalEquipAttachesToNonmatchingCreature() {
        Permanent robe = addRobeReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe),
                0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(robe.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void restrictedEquipAttachesToWarlock() {
        Permanent robe = addRobeReady();
        Permanent warlock = addCreatureReady(player1, new GrimWanderer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe),
                1, null, warlock.getId());
        harness.passBothPriorities();

        assertThat(robe.getAttachedTo()).isEqualTo(warlock.getId());
    }

    @Test
    void restrictedEquipAttachesToShaman() {
        Permanent robe = addRobeReady();
        Permanent shaman = addCreatureReady(player1, new ChaosChanneler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe),
                1, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(robe.getAttachedTo()).isEqualTo(shaman.getId());
    }

    @Test
    void equipmentControllerDrawsWhenOpponentsEquippedCreatureDealsDamage() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent wizard = addCreatureReady(player2, new WizardMentor());
        Permanent robe = addRobeReady();
        robe.setAttachedTo(wizard.getId());
        wizard.setAttacking(true);
        int robeControllerHand = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHand = gd.playerHands.get(player2.getId()).size();

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(robeControllerHand + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHand);
    }

    @Test
    void combatDamageToCreatureDoesNotDrawCards() {
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent robe = addRobeReady();
        robe.setAttachedTo(wizard.getId());
        addCreatureReady(player2, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void unattachedRobeDoesNotTriggerForCombatDamage() {
        addRobeReady();
        Permanent wizard = addCreatureReady(player1, new WizardMentor());
        wizard.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addRobeReady() {
        return addCreatureReady(player1, new RobeOfTheArchmagi());
    }
}
