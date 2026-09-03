package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterApothecary;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldmageElder.class, MasterApothecary.class, FugitiveWizard.class, GrizzlyBears.class, Shock.class})
class ShieldmageElderTest extends BaseCardTest {

    @Test
    @DisplayName("Two Clerics prevent all damage from the target creature")
    void clericAbilityPreventsCreatureDamage() {
        Permanent elder = addCreatureReady(player1, new ShieldmageElder());
        Permanent cleric1 = addCreatureReady(player1, new MasterApothecary());
        addCreatureReady(player1, new MasterApothecary());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        activateAbility(elder, 0, attacker.getId(), elder.getId(), cleric1.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        resolveCombat(player2);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Two Wizards prevent all damage from the target spell")
    void wizardAbilityPreventsSpellDamage() {
        Permanent elder = addCreatureReady(player1, new ShieldmageElder());
        Permanent wizard1 = addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player1, new FugitiveWizard());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        activateAbility(elder, 1, shock.getId(), elder.getId(), wizard1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The Wizard ability can target a creature spell")
    void wizardAbilityTargetsCreatureSpell() {
        Permanent elder = addCreatureReady(player1, new ShieldmageElder());
        Permanent wizard1 = addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player1, new FugitiveWizard());
        Card bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        activateAbility(elder, 1, bears.getId(), elder.getId(), wizard1.getId());
        harness.passBothPriorities();

        assertThat(gd.targetSpellDamagePreventionShields)
                .anyMatch(shield -> shield.spellCardId().equals(bears.getId()));
    }

    private void activateAbility(Permanent elder, int abilityIndex, java.util.UUID targetId,
                                 java.util.UUID firstWizardOrClericId, java.util.UUID secondWizardOrClericId) {
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elder);
        harness.activateAbility(player1, permanentIndex, abilityIndex, null, targetId);
        harness.handlePermanentChosen(player1, firstWizardOrClericId);
        harness.handlePermanentChosen(player1, secondWizardOrClericId);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(firstWizardOrClericId)
                        || permanent.getId().equals(secondWizardOrClericId))
                .allMatch(Permanent::isTapped)).isTrue();
    }
}
