package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LightningProwessTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent auraPerm = new Permanent(new LightningProwess());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return auraPerm;
    }

    private Permanent readyBears() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);
        return bearsPerm;
    }

    @Test
    @DisplayName("Resolving Lightning Prowess attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = readyBears();

        harness.setHand(player1, List.of(new LightningProwess()));
        harness.addMana(player1, ManaColor.RED, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
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
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);
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

        Permanent elfPerm = new Permanent(new LlanowarElves());
        elfPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elfPerm);

        harness.activateAbility(player1, 0, null, elfPerm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Granted ability leaves a 2-toughness creature alive")
    void grantedAbilityDoesNotKillTwoToughnessCreature() {
        Permanent bearsPerm = readyBears();
        enchant(bearsPerm);

        Permanent targetCreature = new Permanent(new GrizzlyBears());
        targetCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(targetCreature);

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
}
