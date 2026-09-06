package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
}
