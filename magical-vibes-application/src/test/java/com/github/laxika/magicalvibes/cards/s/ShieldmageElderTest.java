package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DaruHealer;
import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.InformationDealer;
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

@CardUsed({ShieldmageElder.class, DaruHealer.class, InformationDealer.class, GlorySeeker.class,
        Shock.class, GoblinSharpshooter.class})
class ShieldmageElderTest extends BaseCardTest {

    @Test
    @DisplayName("Two Clerics prevent all damage from the target creature")
    void clericAbilityPreventsCreatureDamage() {
        Permanent elder = addCreatureReady(player1, new ShieldmageElder());
        Permanent cleric1 = addCreatureReady(player1, new DaruHealer());
        addCreatureReady(player1, new DaruHealer());
        Permanent attacker = addCreatureReady(player2, new GlorySeeker());

        activateAbility(elder, 0, attacker.getId(), elder.getId(), cleric1.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        resolveCombat(player2);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The Cleric ability also prevents noncombat damage from the target creature")
    void clericAbilityPreventsNoncombatCreatureDamage() {
        Permanent elder = addCreatureReady(player1, new ShieldmageElder());
        Permanent cleric1 = addCreatureReady(player1, new DaruHealer());
        addCreatureReady(player1, new DaruHealer());
        Permanent shooter = addCreatureReady(player2, new GoblinSharpshooter());

        activateAbility(elder, 0, shooter.getId(), elder.getId(), cleric1.getId());
        harness.passBothPriorities();

        int shooterIndex = gd.playerBattlefields.get(player2.getId()).indexOf(shooter);
        harness.activateAbility(player2, shooterIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Two Wizards prevent all damage from the target spell")
    void wizardAbilityPreventsSpellDamage() {
        Permanent elder = addCreatureReady(player1, new ShieldmageElder());
        Permanent wizard1 = addCreatureReady(player1, new InformationDealer());
        addCreatureReady(player1, new InformationDealer());
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
        Permanent wizard1 = addCreatureReady(player1, new InformationDealer());
        addCreatureReady(player1, new InformationDealer());
        Card glorySeeker = new GlorySeeker();
        harness.setHand(player2, List.of(glorySeeker));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        activateAbility(elder, 1, glorySeeker.getId(), elder.getId(), wizard1.getId());
        harness.passBothPriorities();

        assertThat(gd.targetSpellDamagePreventionShields)
                .anyMatch(shield -> shield.spellCardId().equals(glorySeeker.getId()));
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
