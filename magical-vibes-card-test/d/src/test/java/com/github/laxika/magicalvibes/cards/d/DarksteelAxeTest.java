package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.TurnToSlag;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({DarksteelAxe.class, CarapaceForger.class, Shatter.class, TurnToSlag.class})
class DarksteelAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresMainPhase() {
        harness.addToBattlefield(player1, new DarksteelAxe());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 base + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 base + 0
    }

    @Test
    @DisplayName("Resolving equip attaches Darksteel Axe to target creature")
    void resolvingEquipAttaches() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature loses boost when Darksteel Axe is moved to another creature")
    void creatureLosesBoostWhenReEquipped() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());        // index 0
        Permanent creature1 = addCreatureReady(player1, new CarapaceForger()); // index 1
        Permanent creature2 = addCreatureReady(player1, new CarapaceForger()); // index 2
        axe.setAttachedTo(creature1.getId());

        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);

        // Re-equip to creature2
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost does not affect unequipped creatures")
    void doesNotAffectUnequippedCreatures() {
        Permanent creature1 = addCreatureReady(player1, new CarapaceForger());
        Permanent creature2 = addCreatureReady(player1, new CarapaceForger());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        axe.setAttachedTo(creature1.getId());

        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(2);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new DarksteelAxe());
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipRequiresTwoMana() {
        harness.addToBattlefield(player1, new DarksteelAxe());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedAxeCanEquip() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        axe.tap();
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(axe.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void shatterDoesNotDestroyAxe() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, axe.getId());

        harness.assertOnBattlefield(player1, "Darksteel Axe");
        harness.assertNotInGraveyard(player1, "Darksteel Axe");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equippedCreatureCanDieWhileAxeSurvives() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Carapace Forger");
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        harness.assertOnBattlefield(player1, "Darksteel Axe");
        assertThat(axe.getAttachedTo()).isNull();
    }
}
