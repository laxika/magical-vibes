package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelWreckingBall.class, AvatarOfMight.class, RodOfRuin.class, GrizzlyBears.class})
class SteelWreckingBallTest extends BaseCardTest {

    @Test
    @DisplayName("The enters ability can lethally damage its controller's creature")
    void entersAndKillsOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Steel Wrecking Ball");
    }

    @Test
    @DisplayName("It enters normally when there are no creatures to target")
    void entersWithoutCreatureTargets() {
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Steel Wrecking Ball");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discard is paid immediately and its own artifact is a legal target")
    void discardsAsCostToDestroyOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertNotInHand(player1, "Steel Wrecking Ball");
        harness.assertInGraveyard(player1, "Steel Wrecking Ball");
        harness.assertOnBattlefield(player1, "Rod of Ruin");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rod of Ruin");
        harness.assertInGraveyard(player1, "Rod of Ruin");
    }

    @Test
    @DisplayName("The hand ability requires red mana and cannot discard without paying")
    void cannotActivateWithoutRedMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Steel Wrecking Ball");
        harness.assertNotInGraveyard(player1, "Steel Wrecking Ball");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When it enters, it deals 5 damage to target creature")
    void entersAndDealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Its hand ability destroys target artifact")
    void handAbilityDestroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertInGraveyard(player1, "Steel Wrecking Ball");
    }

    @Test
    @DisplayName("Its hand ability cannot target a creature")
    void handAbilityRejectsCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SteelWreckingBall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Steel Wrecking Ball");
    }
}
