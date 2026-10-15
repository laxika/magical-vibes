package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningAnger.class, RuneclawBear.class, ElvishMystic.class, DarksteelCitadel.class, TitanicGrowth.class})
class BurningAngerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Burning Anger attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new BurningAnger()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burning Anger")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature taps to deal damage equal to its power to a player")
    void grantedAbilityDealsPowerDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability kills a creature with toughness at most the source's power")
    void grantedAbilityKillsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());

        Permanent elves = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());
        elves.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elvish Mystic");
    }

    @Test
    @DisplayName("Damage tracks the enchanted creature's current power")
    void damageUsesCurrentPower() {
        harness.setLife(player2, 20);

        Permanent elves = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        elves.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(elves.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creature loses the granted ability when Burning Anger leaves the battlefield")
    void abilityLostWhenAuraRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Burning Anger cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.setHand(player1, List.of(new BurningAnger()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent artifact = findPermanent(player1, "Darksteel Citadel");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Damage uses power at resolution after a response boosts the creature")
    void powerChangesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Removing the Aura does not stop an already activated ability")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The creature's controller can activate an ability granted by an opponent's Aura")
    void opponentControlsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the granted ability's tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BurningAnger());
        aura.setAttachedTo(bears.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
