package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TibaltTheFiendBlooded;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningProwess.class, GrizzlyBears.class, LlanowarElves.class, FountainOfYouth.class, TibaltTheFiendBlooded.class})
class LightningProwessTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new LightningProwess());
        auraPerm.setAttachedTo(creature.getId());
        return auraPerm;
    }

    private Permanent readyBears() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        return bearsPerm;
    }

    @Test
    @DisplayName("Resolving Lightning Prowess attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = readyBears();

        harness.setHand(player1, List.of(new LightningProwess()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lightning Prowess")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has haste")
    void enchantedCreatureHasHaste() {
        Permanent bearsPerm = readyBears();
        enchant(bearsPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Summoning sick enchanted creature can use the granted tap ability thanks to haste")
    void summoningSickCreatureCanUseGrantedAbility() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchant(bearsPerm);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the granted ability puts it on the stack under the creature's name")
    void grantedAbilityPutsOnStack() {
        Permanent bearsPerm = readyBears();
        enchant(bearsPerm);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Granted ability deals exactly 1 damage, destroying a 1-toughness creature")
    void grantedAbilityDestroysOneToughnessCreature() {
        Permanent bearsPerm = readyBears();
        enchant(bearsPerm);

        Permanent elfPerm = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elfPerm.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, elfPerm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Granted ability leaves a 2-toughness creature alive")
    void grantedAbilityDoesNotKillTwoToughnessCreature() {
        Permanent bearsPerm = readyBears();
        enchant(bearsPerm);

        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        targetCreature.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(targetCreature);
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature loses haste and the granted ability when Lightning Prowess is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = readyBears();
        Permanent auraPerm = enchant(bearsPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lightning Prowess does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = readyBears();
        Permanent otherBears = readyBears();
        enchant(bearsPerm);

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LightningProwess()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The opposing creature's controller can activate the granted ability")
    void opposingCreatureControllerCanActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningProwess()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Lightning Prowess").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted ability resolves after the Aura leaves the battlefield")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent creature = readyBears();
        Permanent aura = enchant(creature);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A tapped creature cannot pay the granted ability's tap cost")
    void tappedCreatureCannotActivateGrantedAbility() {
        Permanent creature = readyBears();
        enchant(creature);
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The granted damage ability cannot target a noncreature artifact")
    void grantedAbilityCannotTargetNonCreatureArtifact() {
        Permanent creature = readyBears();
        enchant(creature);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted ability deals damage to a planeswalker")
    void grantedAbilityDamagesPlaneswalker() {
        Permanent creature = readyBears();
        enchant(creature);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TibaltTheFiendBlooded());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Tibalt, the Fiend-Blooded");
    }

    @Test
    @DisplayName("The granted ability resolves after its creature source leaves")
    void activatedAbilitySurvivesCreatureRemoval() {
        Permanent creature = readyBears();
        enchant(creature);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Lightning Prowess");
        harness.assertInGraveyard(player1, "Lightning Prowess");
    }

    @Test
    @DisplayName("Lightning Prowess goes to the graveyard if its target leaves before resolution")
    void auraDoesNotResolveWithMissingTarget() {
        Permanent creature = readyBears();
        harness.setHand(player1, List.of(new LightningProwess()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Prowess");
        harness.assertInGraveyard(player1, "Lightning Prowess");
        assertThat(gd.stack).isEmpty();
    }
}
