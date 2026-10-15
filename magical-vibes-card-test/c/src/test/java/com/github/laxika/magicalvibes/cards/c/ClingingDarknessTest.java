package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.v.VoyagerStaff;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClingingDarkness.class, ElvesOfDeepShadow.class, VoyagerStaff.class, Watchwolf.class})
class ClingingDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Clinging Darkness attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());

        harness.setHand(player1, List.of(new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clinging Darkness").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Clinging Darkness can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());

        harness.setHand(player1, List.of(new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Clinging Darkness");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature gets -4/-1")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ClingingDarkness());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Clinging Darkness stops affecting the creature when it is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ClingingDarkness());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Clinging Darkness goes to its owner's graveyard when its creature leaves")
    void goesToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());

        harness.setHand(player1, List.of(new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Clinging Darkness");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Clinging Darkness fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());

        harness.setHand(player1, List.of(new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Clinging Darkness");
        harness.assertNotOnBattlefield(player1, "Clinging Darkness");
    }

    @Test
    @DisplayName("Clinging Darkness kills a one-toughness creature and then goes to the graveyard")
    void killsOneToughnessCreature() {
        Permanent creature = addCreatureReady(player2, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Elves of Deep Shadow");
        harness.assertNotOnBattlefield(player2, "Elves of Deep Shadow");
        harness.assertInGraveyard(player1, "Clinging Darkness");
        harness.assertNotOnBattlefield(player1, "Clinging Darkness");
    }

    @Test
    @DisplayName("Multiple copies stack their penalties and affect only the enchanted creature")
    void multipleCopiesStackOnOnlyEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        Permanent otherCreature = addCreatureReady(player2, new Watchwolf());
        harness.setHand(player1, List.of(new ClingingDarkness(), new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
        assertThat(countPermanents(player1, "Clinging Darkness")).isEqualTo(2);
    }

    @Test
    @DisplayName("Clinging Darkness cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new VoyagerStaff());
        harness.setHand(player1, List.of(new ClingingDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
