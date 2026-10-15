package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeteorSword.class, GrizzlyBears.class})
class MeteorSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Meteor Sword destroys target permanent")
    void enteringDestroysTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeteorSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Meteor Sword");
    }

    @Test
    @DisplayName("Meteor Sword cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new MeteorSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature gets plus three plus three")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip three attaches Meteor Sword to a creature you control")
    void equipAttachesToCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Entering Meteor Sword can destroy a noncreature permanent you control")
    void enteringCanDestroyOwnEquipment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        harness.setHand(player1, List.of(new MeteorSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Meteor Sword");
        harness.assertOnBattlefield(player1, "Meteor Sword");
    }

    @Test
    @DisplayName("Re-equipping moves the boost to the new creature")
    void reequippingMovesBoost() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires three mana")
    void equipRequiresThreeMana() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Meteor Sword's entry trigger can destroy itself")
    void entryTriggerCanDestroyItself() {
        Permanent sword = harness.enterBattlefieldAndReturn(player1, new MeteorSword());

        harness.handlePermanentChosen(player1, sword.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Meteor Sword");
        harness.assertInGraveyard(player1, "Meteor Sword");
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresMainPhase() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sword.getAttachedTo()).isNull();
    }
}
